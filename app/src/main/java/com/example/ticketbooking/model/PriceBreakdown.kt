package com.example.ticketbooking.model

data class PriceBreakdown(
    val base: Double,
    val serviceFee: Double,
    val vat: Double,
    val total: Double
)