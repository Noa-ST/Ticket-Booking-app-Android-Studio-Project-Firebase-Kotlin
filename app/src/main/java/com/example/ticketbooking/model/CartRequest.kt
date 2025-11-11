package com.example.ticketbooking.model

import java.io.Serializable

data class CartRequest(
    val showtimeId: String,
    val seatIndices: List<Int>,
    val unitPrice: Double,
    val holdMinutes: Int = 5
) : Serializable
