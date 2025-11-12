package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ticketbooking.R
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.model.Order

class PaymentSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        val order = intent.getSerializableExtra(IntentKeys.ORDER) as? Order
        val showDateLabel = intent.getStringExtra(IntentKeys.SHOW_DATE_LABEL)
        val showTimeLabel = intent.getStringExtra(IntentKeys.SHOW_TIME_LABEL)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvShowtime = findViewById<TextView>(R.id.tvShowtime)
        val tvSeats = findViewById<TextView>(R.id.tvSeats)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val btnHome = findViewById<Button>(R.id.btnHome)

        tvTitle.text = getString(R.string.payment_success_title)
        if (order != null) {
            val friendly = listOfNotNull(showDateLabel, showTimeLabel).joinToString(" · ")
            tvShowtime.text = if (friendly.isNotBlank()) friendly else getString(R.string.payment_showtime_fmt, order.showtimeId)
            tvSeats.text = order.seatIndices.joinToString(", ")
            tvTotal.text = getString(R.string.cart_total_fmt, order.total)
        }

        btnHome.setOnClickListener {
            // Trở về màn chính, xóa stack để tránh quay lại Cart
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}