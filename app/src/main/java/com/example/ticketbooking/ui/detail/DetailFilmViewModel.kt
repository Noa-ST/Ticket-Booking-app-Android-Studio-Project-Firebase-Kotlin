package com.example.ticketbooking.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.model.Film

class DetailFilmViewModel : ViewModel() {
    private val _film = MutableLiveData<Film?>()
    val film: LiveData<Film?> = _film

    fun setFilm(f: Film?) {
        _film.value = f
    }
}