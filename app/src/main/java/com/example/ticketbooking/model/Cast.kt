package com.example.ticketbooking.model

import java.io.Serializable

data class Cast(
    var Picurl: String?=null,
    var Actor: String?=null,
    val name: String,
    val role: String,
    val image: String
    ): Serializable
