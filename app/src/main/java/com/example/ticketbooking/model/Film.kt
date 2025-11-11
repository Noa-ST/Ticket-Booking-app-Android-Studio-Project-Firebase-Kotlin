package com.example.ticketbooking.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Film(
    var Title: String?=null,
    var Description: String?=null,
    var Poster: String?=null,
    var Time: String?=null,
    var Tralier: String?=null,
    var Imdb: Int=0,
    var Year: Int=0,
    var Price: Double=0.0,
    var Genre: ArrayList<String>? = null,
    var Casts: ArrayList<Cast>? = null
    //var Genre: ArrayList<String> = ArrayList(),
    //var Casts: ArrayList<Cast> = ArrayList()
): Parcelable
