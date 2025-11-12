package com.example.ticketbooking

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TicketBookingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Đảm bảo Firebase được khởi tạo trước khi dùng Auth/Database
        FirebaseApp.initializeApp(this)
    }
}

