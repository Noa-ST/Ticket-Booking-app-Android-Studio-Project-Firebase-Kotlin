package com.example.ticketbooking.data.order

import com.example.ticketbooking.model.Order

interface OrderRepository {
    fun createOrder(order: Order, onResult: (Boolean) -> Unit)
}