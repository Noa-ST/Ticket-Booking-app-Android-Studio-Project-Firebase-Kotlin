package com.example.ticketbooking.data.seat

import com.example.ticketbooking.model.SeatLock
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

class FakeSeatLockRepository : SeatLockRepository {
    private val locks = ConcurrentHashMap<String, SeatLock>()

    override fun lockSeats(showtimeId: String, seatIndices: List<Int>, holdMinutes: Int): SeatLock {
        val token = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + holdMinutes * 60_000L
        val lock = SeatLock(token, showtimeId, seatIndices, expiresAt)
        locks[token] = lock
        return lock
    }

    override fun unlock(token: String) {
        locks.remove(token)
    }

    override fun lockSeatsTransactional(
        showtimeId: String,
        seatIndices: List<Int>,
        holdMinutes: Int,
        onResult: (Boolean, SeatLock?) -> Unit
    ) {
        val lock = lockSeats(showtimeId, seatIndices, holdMinutes)
        onResult(true, lock)
    }

    override fun confirmPurchase(token: String, onResult: (Boolean) -> Unit) {
        // Fake: luôn thành công
        locks.remove(token)
        onResult(true)
    }
}