package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ticketbooking.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bỏ đăng nhập ẩn danh: chỉ điều hướng khi người dùng bấm "Bắt đầu"

        binding.startBtn.setOnClickListener {
            // Đảm bảo đã đăng nhập trước khi vào Main
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}