package com.example.ticketbooking.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cast(
    var PicUrl: String? = null,
    var Actor: String? = null,
    val name: String? = null,
    val role: String? = null,
    val image: String? = null
): Parcelable
