package com.example.ticketbooking.model

import java.io.Serializable

data class Order(
    var id: String,
    var userName: String,
    var showtimeId: String,
    var seatIndices: List<Int>,
    var base: Double,
    var serviceFee: Double,
    var vat: Double,
    var total: Double,
    var createdAt: Long,
    var status: String
) : Serializable {
    // Constructor không tham số để Firebase Realtime Database có thể deserialize
    constructor() : this(
        id = "",
        userName = "",
        showtimeId = "",
        seatIndices = emptyList(),
        base = 0.0,
        serviceFee = 0.0,
        vat = 0.0,
        total = 0.0,
        createdAt = 0L,
        status = ""
    )
}