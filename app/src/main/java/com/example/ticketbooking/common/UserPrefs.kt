package com.example.ticketbooking.common

import android.content.Context
import android.content.SharedPreferences

object UserPrefs {
    private const val PREFS_NAME = "ticketbooking_prefs"
    private const val KEY_NAME = "profile_name"
    private const val KEY_USERNAME = "profile_username"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getName(context: Context): String =
        prefs(context).getString(KEY_NAME, "Guest") ?: "Guest"

    fun getUsername(context: Context): String =
        prefs(context).getString(KEY_USERNAME, "guest") ?: "guest"

    fun setProfile(context: Context, name: String, username: String) {
        val safeName = name.trim().ifBlank { "Guest" }
        val safeUsername = username.trim().ifBlank { "guest" }
        prefs(context).edit()
            .putString(KEY_NAME, safeName)
            .putString(KEY_USERNAME, safeUsername)
            .apply()
    }

    fun logout(context: Context) {
        prefs(context).edit()
            .putString(KEY_NAME, "Guest")
            .putString(KEY_USERNAME, "guest")
            .apply()
    }
}