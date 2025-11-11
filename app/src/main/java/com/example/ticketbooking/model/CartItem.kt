package com.example.ticketbooking.model

import java.io.Serializable

data class CartItem(
    val showtimeId: String,
    val token: String,
    val seatIndices: List<Int>,
    val unitPrice: Double,
    val totalPrice: Double,
    val expiresAt: Long
) : Serializable