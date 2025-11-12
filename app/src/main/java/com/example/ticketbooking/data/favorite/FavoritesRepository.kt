package com.example.ticketbooking.data.favorite

import com.example.ticketbooking.model.Film

interface FavoritesRepository {
    fun isFavorite(film: Film): Boolean
    fun toggleFavorite(film: Film): Boolean // returns new state
    fun addFavorite(film: Film)
    fun removeFavorite(film: Film)
    fun getFavoriteKeys(): Set<String>
}