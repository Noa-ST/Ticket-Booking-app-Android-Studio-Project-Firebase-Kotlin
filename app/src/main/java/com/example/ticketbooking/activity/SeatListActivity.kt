package com.example.ticketbooking.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.DateAdapter
import com.example.ticketbooking.adapter.SeatListAdapter
import com.example.ticketbooking.adapter.TimeAdapter
import com.example.ticketbooking.databinding.ActivitySeatListBinding
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.model.Seat
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.ui.seat.SeatViewModel
import com.example.ticketbooking.data.seat.SeatLockRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@AndroidEntryPoint
class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var film: Film
    private var price: Double=0.0
    private var number: Int=0
    private lateinit var viewModel: SeatViewModel
    @Inject lateinit var seatLockRepository: SeatLockRepository
    @Inject lateinit var firebaseDatabase: com.google.firebase.database.FirebaseDatabase
    private lateinit var showtimeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getIntentExtra()
        setVariable()
        viewModel = ViewModelProvider(this)[SeatViewModel::class.java]
        viewModel.attachRepository(seatLockRepository)
        viewModel.attachDatabase(firebaseDatabase)
        bindObservers()
        showtimeId = generateShowtimeId()
        viewModel.observeShowtimeSeats(showtimeId)
        initTimeDateList()
        initSeatsList()
    }

    private fun bindObservers() {
        viewModel.selectedCount.observe(this) { count ->
            binding.numberSelectedTxt.text = getString(R.string.selected_seats, count)
        }
        viewModel.totalPrice.observe(this) { total ->
            binding.priceTxt.text = getString(R.string.price_format, total)
        }
        viewModel.seats.observe(this) { seats ->
            (binding.seatRecyclerview.adapter as? SeatListAdapter)?.updateData(seats)
        }
        viewModel.seatErrorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                // Cho phép người dùng thử lại
                binding.button3.isEnabled = true
            }
        }
        viewModel.isLocked.observe(this) { locked ->
            if (locked) {
                Toast.makeText(this, "Đã giữ ghế trong 5 phút", Toast.LENGTH_SHORT).show()
            }
            binding.seatRecyclerview.isEnabled = !locked
            // Khi đã giữ ghế, vô hiệu hoá nút để tránh nhấn lặp
            binding.button3.isEnabled = !locked
        }
        viewModel.lockCountdownText.observe(this) { text ->
            if (text.isNotBlank()) {
                binding.numberSelectedTxt.text = getString(R.string.selected_seats, viewModel.selectedCount.value ?: 0) + "  (Còn: " + text + ")"
            }
        }
        viewModel.cartItemSummary.observe(this) { item ->
            if (item != null) {
                val intent = android.content.Intent(this, CartActivity::class.java)
                intent.putExtra(IntentKeys.CART_ITEM, item)
                startActivity(intent)
            }
        }
    }

    private fun initSeatsList() {
        val gridLayoutManager= GridLayoutManager(this, 7)
        gridLayoutManager.spanSizeLookup=object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return if(position % 7 == 3) 1 else 1
            }
        }

        binding.apply {
            seatRecyclerview.layoutManager = gridLayoutManager

            // Cấu hình giá vé và danh sách ghế qua ViewModel
            viewModel.setUnitPrice(film.Price)
            viewModel.initSeats(
                numberSeats = 81,
                unavailableIndices = setOf(2, 20, 33, 41, 50, 72, 73)
            )

            val seatAdapter = SeatListAdapter(emptyList(), this@SeatListActivity) { index ->
                viewModel.toggleSeat(index)
            }
            seatRecyclerview.adapter = seatAdapter
            seatRecyclerview.isNestedScrollingEnabled = false
        }
    }

    private fun initTimeDateList() {
        binding.apply {
            dateRecyclerview.layoutManager =
                LinearLayoutManager (this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            dateRecyclerview.adapter= DateAdapter(generateDates())

            timeRecyclerview.layoutManager =
                LinearLayoutManager (this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            timeRecyclerview.adapter= TimeAdapter(generateTimeSlots())
        }
    }
    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }
        binding.button3.setOnClickListener {
            // Điều hướng sang giỏ hàng ngay, CartActivity sẽ tự giữ ghế
            val seats = viewModel.seats.value ?: emptyList()
            val selectedIndices = seats.mapIndexedNotNull { idx, seat ->
                if (seat.status == com.example.ticketbooking.model.Seat.SeatStatus.SELECTED) idx else null
            }
            if (selectedIndices.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ghế trước", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val count = viewModel.selectedCount.value ?: selectedIndices.size
            val total = viewModel.totalPrice.value ?: (film.Price * count)
            val unit = if (count > 0) total / count else film.Price

            val req = com.example.ticketbooking.model.CartRequest(
                showtimeId = showtimeId,
                seatIndices = selectedIndices,
                unitPrice = unit,
                holdMinutes = 5
            )
            val intent = android.content.Intent(this, CartActivity::class.java)
            intent.putExtra(com.example.ticketbooking.common.IntentKeys.CART_REQUEST, req)
            startActivity(intent)
        }
    }
    private fun getIntentExtra() {
        film = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(IntentKeys.FILM, Film::class.java) ?: Film()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(IntentKeys.FILM) ?: Film()
        }
    }
    private fun generateDates(): List<String>{
        val dates=mutableListOf<String>()
        val today= LocalDate.now()
        val formatter= DateTimeFormatter.ofPattern("EEE/dd/MMM")

        for(i in 0 until 7){
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        return dates
    }

    private fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

            for (i in 0 until 24 step 2){
                val time = LocalDate.now().atTime(i, 0)
                timeSlots.add(time.format(formatter))
            }
        return timeSlots
    }

    private fun generateShowtimeId(): String {
        val title = (film.Title ?: "film").ifBlank { "film" }
        // Firebase Realtime Database key không được chứa . $ # [ ] /
        val sanitized = title
            .replace(Regex("[.#$\\[\\]/]"), "-")
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), "-")
        return "$sanitized-${System.currentTimeMillis()}"
    }
}
