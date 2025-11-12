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
    private var showDateLabel: String? = null
    private var showTimeLabel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartItem = intent.getSerializableExtra(com.example.ticketbooking.common.IntentKeys.CART_ITEM) as? com.example.ticketbooking.model.CartItem
        val cartRequest = intent.getSerializableExtra(com.example.ticketbooking.common.IntentKeys.CART_REQUEST) as? com.example.ticketbooking.model.CartRequest
        showDateLabel = intent.getStringExtra(com.example.ticketbooking.common.IntentKeys.SHOW_DATE_LABEL)
        showTimeLabel = intent.getStringExtra(com.example.ticketbooking.common.IntentKeys.SHOW_TIME_LABEL)

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

        // Tránh crash: vô hiệu hóa nút khi chưa có dữ liệu giỏ hàng
        btnPay.isEnabled = cartItem != null
        btnCancel.isEnabled = cartItem != null

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

                        // Lưu đơn hàng trạng thái HELD vào Firebase để hiển thị ở Cart list
                        val heldOrder = Order(
                            id = lock.token, // dùng token làm id tạm
                            userName = "guest",
                            showtimeId = cartRequest.showtimeId,
                            seatIndices = cartRequest.seatIndices,
                            base = 0.0,
                            serviceFee = 0.0,
                            vat = 0.0,
                            total = cartItem!!.totalPrice,
                            createdAt = System.currentTimeMillis(),
                            status = "HELD"
                        )
                        orderRepository.createOrder(heldOrder) { /* ignore result for now */ }
                    } else {
                        android.widget.Toast.makeText(this, "Một số ghế vừa bị chọn bởi người khác. Vui lòng thử lại.", android.widget.Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        btnPay.setOnClickListener {
            val currentItem = cartItem
            if (currentItem == null) {
                Toast.makeText(this, "Đang chuẩn bị dữ liệu giỏ hàng...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            seatLockRepository.confirmPurchase(currentItem.token) { success ->
                runOnUiThread {
                    if (success) {
                        // Tạo đơn hàng sau khi xác nhận thanh toán
                        val breakdown = com.example.ticketbooking.data.pricing.PricingService.computeBreakdown(
                            unitPrice = currentItem.unitPrice,
                            count = currentItem.seatIndices.size,
                            feePercent = 0.05,
                            fixedFee = 1.5,
                            vatPercent = 0.10
                        )
                        // Cập nhật đơn HELD (id = token) sang PAID với đầy đủ breakdown
                        val order = Order(
                            id = currentItem.token,
                            userName = "guest",
                            showtimeId = currentItem.showtimeId,
                            seatIndices = currentItem.seatIndices,
                            base = breakdown.base,
                            serviceFee = breakdown.serviceFee,
                            vat = breakdown.vat,
                            total = breakdown.total,
                            createdAt = System.currentTimeMillis(),
                            status = "PAID"
                        )
                        orderRepository.updateOrder(order) { ok ->
                            runOnUiThread {
                                if (ok) {
                                    // Điều hướng tới màn xác nhận thanh toán
                                    val intent = android.content.Intent(this, PaymentSuccessActivity::class.java)
                                    intent.putExtra(com.example.ticketbooking.common.IntentKeys.ORDER, order)
                                    // Truyền tiếp nhãn ngày/giờ để hiển thị ở màn thành công
                                    showDateLabel?.let { intent.putExtra(com.example.ticketbooking.common.IntentKeys.SHOW_DATE_LABEL, it) }
                                    showTimeLabel?.let { intent.putExtra(com.example.ticketbooking.common.IntentKeys.SHOW_TIME_LABEL, it) }
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Tạo đơn hàng thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Thanh toán thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            val currentItem = cartItem
            if (currentItem == null) {
                Toast.makeText(this, "Không có dữ liệu để hủy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            seatLockRepository.unlock(currentItem.token)
            // Cập nhật đơn HELD thành CANCELLED để không hiển thị là đang giữ chỗ
            val cancelled = Order(
                id = currentItem.token,
                userName = "guest",
                showtimeId = currentItem.showtimeId,
                seatIndices = currentItem.seatIndices,
                base = 0.0,
                serviceFee = 0.0,
                vat = 0.0,
                total = currentItem.totalPrice,
                createdAt = System.currentTimeMillis(),
                status = "CANCELLED"
            )
            orderRepository.updateOrder(cancelled) { }
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
                // Nếu hết thời gian, đánh dấu đơn là EXPIRED
                val currentItem = cartItem
                if (currentItem != null) {
                    val expired = Order(
                        id = currentItem.token,
                        userName = "guest",
                        showtimeId = currentItem.showtimeId,
                        seatIndices = currentItem.seatIndices,
                        base = 0.0,
                        serviceFee = 0.0,
                        vat = 0.0,
                        total = currentItem.totalPrice,
                        createdAt = System.currentTimeMillis(),
                        status = "EXPIRED"
                    )
                    orderRepository.updateOrder(expired) { }
                }
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
        // Hiển thị thông tin suất chiếu thân thiện nếu có
        val friendly = listOfNotNull(showDateLabel, showTimeLabel).joinToString(" · ")
        tvShowtime.text = if (friendly.isNotBlank()) friendly else getString(R.string.cart_showtime_fmt, item.showtimeId)
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
        // Sau khi render, cho phép hủy và tuỳ thời gian cho phép thanh toán
        findViewById<android.widget.Button>(R.id.btnCancel).isEnabled = true
    }
}