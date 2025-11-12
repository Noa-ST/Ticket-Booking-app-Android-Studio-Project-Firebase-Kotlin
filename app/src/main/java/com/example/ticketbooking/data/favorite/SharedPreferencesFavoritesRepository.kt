package com.example.ticketbooking.data.favorite

import android.content.SharedPreferences
import com.example.ticketbooking.model.Film

class SharedPreferencesFavoritesRepository(
    private val prefs: SharedPreferences
) : FavoritesRepository {

    private val KEY_SET = "favorites"

    private fun keyFor(film: Film): String {
        val title = (film.Title ?: "film").ifBlank { "film" }
        val sanitized = title
            .replace(Regex("[.#$\\[\\]/]"), "-")
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), "-")
        return "$sanitized-${film.Year}"
    }

    override fun isFavorite(film: Film): Boolean {
        val set = prefs.getStringSet(KEY_SET, emptySet()) ?: emptySet()
        return set.contains(keyFor(film))
    }

    override fun toggleFavorite(film: Film): Boolean {
        val key = keyFor(film)
        val current = prefs.getStringSet(KEY_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
        val nowFav: Boolean
        if (current.contains(key)) {
            current.remove(key)
            nowFav = false
        } else {
            current.add(key)
            nowFav = true
        }
        prefs.edit().putStringSet(KEY_SET, current).apply()
        return nowFav
    }

    override fun addFavorite(film: Film) {
        val key = keyFor(film)
        val current = prefs.getStringSet(KEY_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(key)
        prefs.edit().putStringSet(KEY_SET, current).apply()
    }

    override fun removeFavorite(film: Film) {
        val key = keyFor(film)
        val current = prefs.getStringSet(KEY_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(key)
        prefs.edit().putStringSet(KEY_SET, current).apply()
    }

    override fun getFavoriteKeys(): Set<String> = prefs.getStringSet(KEY_SET, emptySet()) ?: emptySet()
}