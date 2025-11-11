package com.example.ticketbooking.data.seat

import com.example.ticketbooking.model.SeatLock

interface SeatLockRepository {
    fun lockSeats(showtimeId: String, seatIndices: List<Int>, holdMinutes: Int): SeatLock
    fun unlock(token: String)

    /**
     * Thử lock ghế với transaction để chống conflict.
     * Kết quả trả về qua callback: success=true nếu lock thành công, cùng SeatLock.
     */
    fun lockSeatsTransactional(
        showtimeId: String,
        seatIndices: List<Int>,
        holdMinutes: Int,
        onResult: (Boolean, SeatLock?) -> Unit
    )

    /**
     * Xác nhận thanh toán: chuyển ghế từ trạng thái reserved sang SOLD.
     */
    fun confirmPurchase(
        token: String,
        onResult: (Boolean) -> Unit
    )
}