package com.example.ticketbooking.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.model.SliderItems
import com.example.ticketbooking.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: FilmRepository) : ViewModel() {

    private val _banners = MutableLiveData<UiState<List<SliderItems>>>()
    val banners: LiveData<UiState<List<SliderItems>>> = _banners

    private val _topMovies = MutableLiveData<UiState<List<Film>>>()
    val topMovies: LiveData<UiState<List<Film>>> = _topMovies

    private val _upcoming = MutableLiveData<UiState<List<Film>>>()
    val upcoming: LiveData<UiState<List<Film>>> = _upcoming

    fun loadBanners() {
        _banners.value = UiState.Loading
        repository.getBanners(
            onSuccess = { _banners.value = UiState.Success(it) },
            onError = { _banners.value = UiState.Error(it) }
        )
    }

    fun loadTopMovies() {
        _topMovies.value = UiState.Loading
        repository.getTopMovies(
            onSuccess = { _topMovies.value = UiState.Success(it) },
            onError = { _topMovies.value = UiState.Error(it) }
        )
    }

    fun loadUpcoming() {
        _upcoming.value = UiState.Loading
        repository.getUpcoming(
            onSuccess = { _upcoming.value = UiState.Success(it) },
            onError = { _upcoming.value = UiState.Error(it) }
        )
    }
}