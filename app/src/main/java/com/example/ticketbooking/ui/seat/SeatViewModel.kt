package com.example.ticketbooking.ui.seat

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.model.Seat
import com.example.ticketbooking.data.seat.SeatLockRepository
import com.example.ticketbooking.model.SeatLock
import com.example.ticketbooking.model.CartItem
import com.example.ticketbooking.data.pricing.PricingService
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SeatViewModel : ViewModel() {
    private val _selectedCount = MutableLiveData(0)
    val selectedCount: LiveData<Int> = _selectedCount

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private val _seats = MutableLiveData<List<Seat>>(emptyList())
    val seats: LiveData<List<Seat>> = _seats

    private var unitPrice: Double = 0.0

    private val _seatErrorMessage = MutableLiveData<String?>(null)
    val seatErrorMessage: LiveData<String?> = _seatErrorMessage

    // Seat Locking
    private val _isLocked = MutableLiveData(false)
    val isLocked: LiveData<Boolean> = _isLocked

    private val _lockCountdownText = MutableLiveData("")
    val lockCountdownText: LiveData<String> = _lockCountdownText

    private var countDownTimer: CountDownTimer? = null
    private var currentLock: SeatLock? = null
    private var seatLockRepository: SeatLockRepository? = null
    private var database: FirebaseDatabase? = null

    private val _cartItemSummary = MutableLiveData<CartItem?>(null)
    val cartItemSummary: LiveData<CartItem?> = _cartItemSummary

    fun attachRepository(repo: SeatLockRepository) {
        seatLockRepository = repo
    }

    fun attachDatabase(db: FirebaseDatabase) {
        database = db
    }

    fun setUnitPrice(price: Double) {
        unitPrice = price
        recalcTotals()
    }

    fun initSeats(numberSeats: Int, unavailableIndices: Set<Int>) {
        val generated = MutableList(numberSeats) { index ->
            val status = if (unavailableIndices.contains(index)) Seat.SeatStatus.UNAVAILABLE
            else Seat.SeatStatus.AVAILABLE
            Seat(status, "")
        }
        _seats.value = generated
        recalcTotals()
    }

    fun observeShowtimeSeats(showtimeId: String) {
        val db = database ?: return
        db.getReference("ShowtimeSeats").child(showtimeId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = snapshot.value as? Map<String, Any?> ?: emptyMap()
                    val current = _seats.value?.toMutableList() ?: return
                    // Đánh dấu ghế có entry (token hoặc "SOLD") là UNAVAILABLE
                    map.forEach { (key, value) ->
                        val idx = key.toIntOrNull()
                        if (idx != null && idx in current.indices) {
                            current[idx] = current[idx].copy(status = Seat.SeatStatus.UNAVAILABLE)
                        }
                    }
                    _seats.postValue(current)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Không làm gì, có thể log
                }
            })
    }

    fun toggleSeat(index: Int) {
        if (_isLocked.value == true) return
        val current = _seats.value ?: return
        if (index !in current.indices) return
        val seat = current[index]
        val newStatus = when (seat.status) {
            Seat.SeatStatus.AVAILABLE -> Seat.SeatStatus.SELECTED
            Seat.SeatStatus.SELECTED -> Seat.SeatStatus.AVAILABLE
            Seat.SeatStatus.UNAVAILABLE -> Seat.SeatStatus.UNAVAILABLE
        }
        if (newStatus == Seat.SeatStatus.UNAVAILABLE) return

        val updated = current.toMutableList()
        updated[index] = seat.copy(status = newStatus)

        // Validate single-seat-gap when selecting
        if (seat.status == Seat.SeatStatus.AVAILABLE && newStatus == Seat.SeatStatus.SELECTED) {
            if (violatesSingleSeatGap(updated, index, columns = 7)) {
                _seatErrorMessage.value = "Không thể để trống một ghế lẻ giữa cặp."
                return
            }
        }

        _seats.value = updated
        recalcTotals()
    }

    private fun recalcTotals() {
        val count = _seats.value?.count { it.status == Seat.SeatStatus.SELECTED } ?: 0
        _selectedCount.value = count
        _totalPrice.value = PricingService.computeTotal(unitPrice, count)
    }

    private fun violatesSingleSeatGap(list: List<Seat>, index: Int, columns: Int): Boolean {
        val rowStart = (index / columns) * columns
        val rowEnd = rowStart + columns - 1

        fun statusAt(i: Int): Seat.SeatStatus? {
            return if (i in rowStart..rowEnd) list[i].status else null
        }

        // Left side: [X][A][S]
        val left = index - 1
        val left2 = index - 2
        val leftGap = (left in rowStart..rowEnd
                && statusAt(left) == Seat.SeatStatus.AVAILABLE
                && (left2 !in rowStart..rowEnd || statusAt(left2) != Seat.SeatStatus.AVAILABLE)
                && list[index].status == Seat.SeatStatus.SELECTED)

        // Right side: [S][A][X]
        val right = index + 1
        val right2 = index + 2
        val rightGap = (right in rowStart..rowEnd
                && statusAt(right) == Seat.SeatStatus.AVAILABLE
                && (right2 !in rowStart..rowEnd || statusAt(right2) != Seat.SeatStatus.AVAILABLE)
                && list[index].status == Seat.SeatStatus.SELECTED)

        return leftGap || rightGap
    }

    fun lockSelectedSeats(showtimeId: String, holdMinutes: Int = 5) {
        val repo = seatLockRepository ?: return
        val seatsNow = _seats.value ?: return
        val selectedIndices = seatsNow.mapIndexedNotNull { idx, seat ->
            if (seat.status == Seat.SeatStatus.SELECTED) idx else null
        }
        if (selectedIndices.isEmpty()) {
            _seatErrorMessage.value = "Vui lòng chọn ghế trước khi giữ chỗ."
            return
        }
        repo.lockSeatsTransactional(showtimeId, selectedIndices, holdMinutes) { success, lock ->
            if (!success || lock == null) {
                _seatErrorMessage.postValue("Một số ghế vừa bị chọn bởi người khác. Vui lòng thử lại.")
                return@lockSeatsTransactional
            }
            currentLock = lock
            _isLocked.postValue(true)
            startCountdown(lock.expiresAt)
            val total = _totalPrice.value ?: (unitPrice * selectedIndices.size)
            _cartItemSummary.postValue(
                CartItem(
                    showtimeId = showtimeId,
                    token = lock.token,
                    seatIndices = selectedIndices,
                    unitPrice = unitPrice,
                    totalPrice = total,
                    expiresAt = lock.expiresAt
                )
            )
        }
    }

    private fun startCountdown(expiresAt: Long) {
        countDownTimer?.cancel()
        val remaining = expiresAt - System.currentTimeMillis()
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _lockCountdownText.value = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                _isLocked.value = false
                _lockCountdownText.value = ""
                // Release selection on expiry
                val updated = _seats.value?.map {
                    if (it.status == Seat.SeatStatus.SELECTED) it.copy(status = Seat.SeatStatus.AVAILABLE) else it
                }
                _seats.value = updated
                recalcTotals()
                currentLock?.let { seatLockRepository?.unlock(it.token) }
                currentLock = null
                _cartItemSummary.value = null
            }
        }
        countDownTimer?.start()
    }

    fun cancelLock() {
        countDownTimer?.cancel()
        _isLocked.value = false
        _lockCountdownText.value = ""
        currentLock?.let { seatLockRepository?.unlock(it.token) }
        currentLock = null
        _cartItemSummary.value = null
    }
}