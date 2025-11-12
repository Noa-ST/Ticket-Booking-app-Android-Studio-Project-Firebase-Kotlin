package com.example.ticketbooking.data

import com.example.ticketbooking.model.Film
import com.example.ticketbooking.model.SliderItems
import com.google.firebase.database.*

class FilmRepository(private val database: FirebaseDatabase = FirebaseDatabase.getInstance()) {
    private var cachedTop: List<Film>? = null
    private var cachedUpcoming: List<Film>? = null
    private var topTimestamp: Long = 0L
    private var upcomingTimestamp: Long = 0L
    private val ttlMs: Long = 5 * 60 * 1000 // 5 minutes

    fun getTopMovies(
        onSuccess: (List<Film>) -> Unit,
        onError: (String) -> Unit
    ) {
        val now = System.currentTimeMillis()
        cachedTop?.let { list ->
            if (now - topTimestamp <= ttlMs) {
                onSuccess(list)
                return
            }
        }
        val ref: DatabaseReference = database.getReference("Items")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = ArrayList<Film>()
                for (i in snapshot.children) {
                    i.getValue(Film::class.java)?.let { items.add(it) }
                }
                cachedTop = items
                topTimestamp = System.currentTimeMillis()
                onSuccess(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message ?: "Unknown error")
            }
        })
    }

    fun getUpcoming(
        onSuccess: (List<Film>) -> Unit,
        onError: (String) -> Unit
    ) {
        val now = System.currentTimeMillis()
        cachedUpcoming?.let { list ->
            if (now - upcomingTimestamp <= ttlMs) {
                onSuccess(list)
                return
            }
        }
        val ref: DatabaseReference = database.getReference("Upcomming")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = ArrayList<Film>()
                for (i in snapshot.children) {
                    i.getValue(Film::class.java)?.let { items.add(it) }
                }
                cachedUpcoming = items
                upcomingTimestamp = System.currentTimeMillis()
                onSuccess(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message ?: "Unknown error")
            }
        })
    }

    fun getBanners(
        onSuccess: (List<SliderItems>) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref: DatabaseReference = database.getReference("Banners")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = ArrayList<SliderItems>()
                for (i in snapshot.children) {
                    i.getValue(SliderItems::class.java)?.let { lists.add(it) }
                }
                onSuccess(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message ?: "Unknown error")
            }
        })
    }
}