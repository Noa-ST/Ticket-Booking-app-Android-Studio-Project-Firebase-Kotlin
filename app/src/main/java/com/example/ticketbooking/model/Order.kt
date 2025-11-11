package com.example.ticketbooking.model

data class Order(
    val id: String,
    val userName: String,
    val showtimeId: String,
    val seatIndices: List<Int>,
    val base: Double,
    val serviceFee: Double,
    val vat: Double,
    val total: Double,
    val createdAt: Long,
    val status: String
)