package com.example.ticketbooking.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.data.order.OrderRepository
import com.example.ticketbooking.data.seat.SeatLockRepository
import com.example.ticketbooking.model.Order
import com.example.ticketbooking.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val seatLockRepository: SeatLockRepository
) : ViewModel() {

    private val _orders = MutableLiveData<UiState<List<Order>>>()
    val orders: LiveData<UiState<List<Order>>> = _orders

    fun loadOrders(userName: String = "guest") {
        _orders.value = UiState.Loading
        repository.getOrdersByUser(
            userName,
            onSuccess = { _orders.value = UiState.Success(it.sortedByDescending { o -> o.createdAt }) },
            onError = { _orders.value = UiState.Error(it) }
        )
    }

    fun cancelOrder(order: Order, onDone: (Boolean) -> Unit) {
        try {
            // Bỏ giữ ghế nếu còn HELD
            if (order.status == "HELD") {
                seatLockRepository.unlock(order.id)
            }
            val cancelled = order.copy(status = "CANCELLED")
            repository.updateOrder(cancelled) { ok ->
                onDone(ok)
            }
        } catch (e: Exception) {
            onDone(false)
        }
    }
}