package com.example.ticketbooking.data.order

import com.example.ticketbooking.model.Order
import com.google.firebase.database.FirebaseDatabase

class FirebaseOrderRepository(private val database: FirebaseDatabase) : OrderRepository {
    override fun createOrder(order: Order, onResult: (Boolean) -> Unit) {
        val ref = database.getReference("Orders").child(order.id)
        ref.setValue(order)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}