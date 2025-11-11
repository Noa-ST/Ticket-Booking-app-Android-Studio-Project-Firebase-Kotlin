package com.example.ticketbooking.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ticketbooking.R
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.data.seat.SeatLockRepository
import com.example.ticketbooking.model.CartItem
import com.example.ticketbooking.model.Order
import com.example.ticketbooking.data.order.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CartActivity : AppCompatActivity() {
    @Inject lateinit var seatLockRepository: SeatLockRepository
    @Inject lateinit var orderRepository: OrderRepository

    private var countDownTimer: CountDownTimer? = null
    private var cartItem: com.example.ticketbooking.model.CartItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartItem = intent.getSerializableExtra(com.example.ticketbooking.common.IntentKeys.CART_ITEM) as? com.example.ticketbooking.model.CartItem
        val cartRequest = intent.getSerializableExtra(com.example.ticketbooking.common.IntentKeys.CART_REQUEST) as? com.example.ticketbooking.model.CartRequest

        if (cartItem == null && cartRequest == null) {
            android.widget.Toast.makeText(this, "Không có dữ liệu giỏ hàng", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvShowtime = findViewById<android.widget.TextView>(R.id.tvShowtime)
        val tvSeats = findViewById<android.widget.TextView>(R.id.tvSeats)
        val tvBase = findViewById<android.widget.TextView>(R.id.tvBase)
        val tvServiceFee = findViewById<android.widget.TextView>(R.id.tvServiceFee)
        val tvVat = findViewById<android.widget.TextView>(R.id.tvVat)
        val tvTotal = findViewById<android.widget.TextView>(R.id.tvTotal)
        val tvCountdown = findViewById<android.widget.TextView>(R.id.tvCountdown)
        val btnPay = findViewById<android.widget.Button>(R.id.btnPay)
        val btnCancel = findViewById<android.widget.Button>(R.id.btnCancel)

        if (cartItem != null) {
            renderCart(cartItem!!, tvShowtime, tvSeats, tvBase, tvServiceFee, tvVat, tvTotal, tvCountdown, btnPay)
        } else if (cartRequest != null) {
            // Giữ ghế khi vào Cart, nếu thành công thì render
            seatLockRepository.lockSeatsTransactional(
                showtimeId = cartRequest.showtimeId,
                seatIndices = cartRequest.seatIndices,
                holdMinutes = cartRequest.holdMinutes
            ) { success, lock ->
                runOnUiThread {
                    if (success && lock != null) {
                        cartItem = com.example.ticketbooking.model.CartItem(
                            showtimeId = cartRequest.showtimeId,
                            token = lock.token,
                            seatIndices = cartRequest.seatIndices,
                            unitPrice = cartRequest.unitPrice,
                            totalPrice = com.example.ticketbooking.data.pricing.PricingService.computeTotal(cartRequest.unitPrice, cartRequest.seatIndices.size),
                            expiresAt = lock.expiresAt
                        )
                        renderCart(cartItem!!, tvShowtime, tvSeats, tvBase, tvServiceFee, tvVat, tvTotal, tvCountdown, btnPay)
                    } else {
                        android.widget.Toast.makeText(this, "Một số ghế vừa bị chọn bởi người khác. Vui lòng thử lại.", android.widget.Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        btnPay.setOnClickListener {
            seatLockRepository.confirmPurchase(cartItem!!.token) { success ->
                runOnUiThread {
                    if (success) {
                        // Tạo đơn hàng sau khi xác nhận thanh toán
                        val breakdown = com.example.ticketbooking.data.pricing.PricingService.computeBreakdown(
                            unitPrice = cartItem!!.unitPrice,
                            count = cartItem!!.seatIndices.size,
                            feePercent = 0.05,
                            fixedFee = 1.5,
                            vatPercent = 0.10
                        )
                        val order = Order(
                            id = java.util.UUID.randomUUID().toString(),
                            userName = "guest",
                            showtimeId = cartItem!!.showtimeId,
                            seatIndices = cartItem!!.seatIndices,
                            base = breakdown.base,
                            serviceFee = breakdown.serviceFee,
                            vat = breakdown.vat,
                            total = breakdown.total,
                            createdAt = System.currentTimeMillis(),
                            status = "PAID"
                        )
                        orderRepository.createOrder(order) { ok ->
                            runOnUiThread {
                                Toast.makeText(this, if (ok) "Thanh toán thành công!" else "Tạo đơn hàng thất bại", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Thanh toán thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            seatLockRepository.unlock(cartItem!!.token)
            Toast.makeText(this, "Đã hủy giữ chỗ", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCountdown(expiresAt: Long, tv: TextView, payButton: Button) {
        countDownTimer?.cancel()
        val remaining = expiresAt - System.currentTimeMillis()
        payButton.isEnabled = remaining > 0
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tv.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                payButton.isEnabled = false
                Toast.makeText(this@CartActivity, "Hết thời gian giữ chỗ", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        countDownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun renderCart(
        item: com.example.ticketbooking.model.CartItem,
        tvShowtime: android.widget.TextView,
        tvSeats: android.widget.TextView,
        tvBase: android.widget.TextView,
        tvServiceFee: android.widget.TextView,
        tvVat: android.widget.TextView,
        tvTotal: android.widget.TextView,
        tvCountdown: android.widget.TextView,
        btnPay: android.widget.Button
    ) {
        tvShowtime.text = getString(R.string.cart_showtime_fmt, item.showtimeId)
        tvSeats.text = item.seatIndices.joinToString(", ")

        val breakdown = com.example.ticketbooking.data.pricing.PricingService.computeBreakdown(
            unitPrice = item.unitPrice,
            count = item.seatIndices.size,
            feePercent = 0.05,
            fixedFee = 1.5,
            vatPercent = 0.10
        )
        tvBase.text = getString(R.string.cart_base_fmt, breakdown.base)
        tvServiceFee.text = getString(R.string.cart_service_fee_fmt, breakdown.serviceFee)
        tvVat.text = getString(R.string.cart_vat_fmt, breakdown.vat)
        tvTotal.text = getString(R.string.cart_total_fmt, breakdown.total)

        startCountdown(item.expiresAt, tvCountdown, btnPay)
    }
}