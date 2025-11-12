package com.example.ticketbooking.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.databinding.ActivitySeeAllBinding
import com.example.ticketbooking.adapter.FilmListAdapter

class SeeAllActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeAllBinding
    private val filmRepo = FilmRepository()
    private var allFilms: List<Film> = emptyList()
    private var displayed: MutableList<Film> = mutableListOf()
    private val pageSize = 20
    private var loadingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = com.example.ticketbooking.adapter.SkeletonFilmAdapter(6)
        binding.progressBar.visibility = View.GONE

        binding.backButton.setOnClickListener { finish() }

        val category = intent.getStringExtra(IntentKeys.CATEGORY)
        binding.titleText.text = when (category) {
            CATEGORY_TOP -> "Top phim"
            CATEGORY_UPCOMING -> "Sắp phát hành"
            else -> "Phim"
        }

        loadData(category)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (!loadingMore && lastVisible >= displayed.size - 4) {
                    loadMore()
                }
            }
        })
    }

    private fun loadData(category: String?) {
        when (category) {
            CATEGORY_TOP -> filmRepo.getTopMovies(
                onSuccess = { films -> showFilms(films) },
                onError = { _ -> showFilms(emptyList()) }
            )
            CATEGORY_UPCOMING -> filmRepo.getUpcoming(
                onSuccess = { films -> showFilms(films) },
                onError = { _ -> showFilms(emptyList()) }
            )
            else -> {
                // fallback: show all combined unique
                filmRepo.getTopMovies(
                    onSuccess = { top ->
                        filmRepo.getUpcoming(
                            onSuccess = { upcoming ->
                                showFilms(uniqueFilms(top + upcoming))
                            },
                            onError = { _ -> showFilms(uniqueFilms(top)) }
                        )
                    },
                    onError = { _ ->
                        filmRepo.getUpcoming(
                            onSuccess = { upcoming -> showFilms(uniqueFilms(upcoming)) },
                            onError = { _ -> showFilms(emptyList()) }
                        )
                    }
                )
            }
        }
    }

    private fun showFilms(films: List<Film>) {
        binding.progressBar.visibility = View.GONE
        allFilms = films
        displayed.clear()
        val initial = allFilms.take(pageSize)
        displayed.addAll(initial)
        binding.recyclerView.adapter = FilmListAdapter(displayed.toList())
        binding.emptyView.visibility = if (allFilms.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadMore() {
        if (displayed.size >= allFilms.size) return
        loadingMore = true
        val nextEnd = (displayed.size + pageSize).coerceAtMost(allFilms.size)
        val nextChunk = allFilms.subList(displayed.size, nextEnd)
        displayed.addAll(nextChunk)
        binding.recyclerView.adapter = FilmListAdapter(displayed.toList())
        loadingMore = false
    }

    private fun uniqueFilms(list: List<Film>): List<Film> {
        return list.distinctBy { (it.Title?.lowercase() ?: "") + "_" + (it.Year ?: "") }
    }

    companion object {
        const val CATEGORY_TOP = "TOP"
        const val CATEGORY_UPCOMING = "UPCOMING"
    }
}