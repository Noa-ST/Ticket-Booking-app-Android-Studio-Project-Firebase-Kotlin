package com.example.ticketbooking.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.data.favorite.FavoritesRepository
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _favorites = MutableLiveData<UiState<List<Film>>>()
    val favorites: LiveData<UiState<List<Film>>> = _favorites

    fun loadFavorites() {
        _favorites.value = UiState.Loading

        // Lấy cả TopMovies và Upcoming rồi lọc theo trạng thái yêu thích
        filmRepository.getTopMovies(
            onSuccess = { top ->
                filmRepository.getUpcoming(
                    onSuccess = { upcoming ->
                        val all = (top + upcoming)
                            .distinctBy { (it.Title ?: "") + "-" + (it.Year ?: "") }
                        val favs = all.filter { favoritesRepository.isFavorite(it) }
                        _favorites.value = UiState.Success(favs)
                    },
                    onError = { err -> _favorites.value = UiState.Error(err) }
                )
            },
            onError = { err -> _favorites.value = UiState.Error(err) }
        )
    }

    fun toggleFavorite(film: Film) {
        // Update storage and optimistically update current list
        favoritesRepository.toggleFavorite(film)
        val current = _favorites.value
        if (current is UiState.Success) {
            val updated = current.data.filterNot {
                (it.Title ?: "") == (film.Title ?: "") && (it.Year ?: "") == (film.Year ?: "")
            }
            _favorites.value = UiState.Success(updated)
        } else {
            // fallback: reload from repository sources
            loadFavorites()
        }
    }
}