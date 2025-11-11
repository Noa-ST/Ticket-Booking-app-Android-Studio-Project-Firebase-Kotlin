package com.example.ticketbooking.model

data class SeatLock(
    val token: String,
    val showtimeId: String,
    val seatIndices: List<Int>,
    val expiresAt: Long
)