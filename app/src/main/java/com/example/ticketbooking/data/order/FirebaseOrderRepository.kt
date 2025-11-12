package com.example.ticketbooking.data.order

import com.example.ticketbooking.model.Order
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class FirebaseOrderRepository(private val database: FirebaseDatabase) : OrderRepository {
    override fun createOrder(order: Order, onResult: (Boolean) -> Unit) {
        val ref = database.getReference("Orders").child(order.id)
        ref.setValue(order)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("FirebaseOrderRepository", "Create order failed: ${e.message}", e)
                onResult(false)
            }
    }

    override fun updateOrder(order: Order, onResult: (Boolean) -> Unit) {
        val ref = database.getReference("Orders").child(order.id)
        ref.setValue(order)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e("FirebaseOrderRepository", "Update order failed: ${e.message}", e)
                onResult(false)
            }
    }

    override fun getOrdersByUser(
        userName: String,
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = database.getReference("Orders")
        ref.get()
            .addOnSuccessListener { snapshot ->
                val orders = mutableListOf<Order>()
                snapshot.children.forEach { child ->
                    child.getValue(Order::class.java)?.let { orders.add(it) }
                }
                onSuccess(orders.filter { it.userName == userName })
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOrderRepository", "Get orders failed: ${e.message}", e)
                onError(e.message ?: "Unknown error")
            }
    }
}