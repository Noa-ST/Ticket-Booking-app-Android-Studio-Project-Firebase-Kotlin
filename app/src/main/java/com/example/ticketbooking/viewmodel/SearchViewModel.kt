package com.example.ticketbooking.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.model.Film

class SearchViewModel : ViewModel() {
    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val _selectedGenres = MutableLiveData<Set<String>>(emptySet())
    val selectedGenres: LiveData<Set<String>> = _selectedGenres

    private val _yearStart = MutableLiveData<Int?>(null)
    val yearStart: LiveData<Int?> = _yearStart

    private val _yearEnd = MutableLiveData<Int?>(null)
    val yearEnd: LiveData<Int?> = _yearEnd

    private val _allFilms = MutableLiveData<List<Film>>(emptyList())
    val allFilms: LiveData<List<Film>> = _allFilms

    private val _results = MutableLiveData<List<Film>>(emptyList())
    val results: LiveData<List<Film>> = _results

    fun loadAllFilms(repository: FilmRepository, onReady: (() -> Unit)? = null) {
        repository.getTopMovies(
            onSuccess = { top ->
                repository.getUpcoming(
                    onSuccess = { upcoming ->
                        _allFilms.value = uniqueFilms(top + upcoming)
                        applyFilters()
                        onReady?.invoke()
                    },
                    onError = { _ ->
                        _allFilms.value = uniqueFilms(top)
                        applyFilters()
                        onReady?.invoke()
                    }
                )
            },
            onError = { _ ->
                repository.getUpcoming(
                    onSuccess = { upcoming ->
                        _allFilms.value = uniqueFilms(upcoming)
                        applyFilters()
                        onReady?.invoke()
                    },
                    onError = { _ ->
                        _allFilms.value = emptyList()
                        applyFilters()
                        onReady?.invoke()
                    }
                )
            }
        )
    }

    fun setQuery(newQuery: String) {
        _query.value = newQuery
        applyFilters()
    }

    fun setGenres(genres: Set<String>) {
        _selectedGenres.value = genres
        applyFilters()
    }

    fun setYearRange(start: Int?, end: Int?) {
        _yearStart.value = start
        _yearEnd.value = end
        applyFilters()
    }

    private fun applyFilters() {
        val all = _allFilms.value ?: emptyList()
        val q = (_query.value ?: "").lowercase()
        val genres = _selectedGenres.value ?: emptySet()
        val start = _yearStart.value
        val end = _yearEnd.value

        var filtered = all
        if (q.isNotBlank()) {
            filtered = filtered.filter { (it.Title ?: "").lowercase().contains(q) }
        }
        if (genres.isNotEmpty()) {
            val gs = genres.map { it.lowercase() }.toSet()
            filtered = filtered.filter { film ->
                (film.Genre ?: arrayListOf()).any { it.trim().lowercase() in gs }
            }
        }
        if (start != null || end != null) {
            filtered = filtered.filter { film ->
                val y = film.Year
                val okStart = start?.let { y >= it } ?: true
                val okEnd = end?.let { y <= it } ?: true
                okStart && okEnd
            }
        }
        _results.value = filtered
    }

    private fun uniqueFilms(list: List<Film>): List<Film> {
        return list.distinctBy { film ->
            val titleKey = (film.Title ?: "").lowercase().trim()
            val yearKey = film.Year
            val posterKey = (film.Poster ?: "").lowercase().trim()
            if (titleKey.isNotEmpty()) "${titleKey}|${yearKey}" else "poster:${posterKey}"
        }
    }
}