package com.example.ticketbooking.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticketbooking.adapter.FavoritesAdapter
import com.example.ticketbooking.adapter.SkeletonFilmAdapter
import com.example.ticketbooking.databinding.ActivityFavoritesBinding
import com.example.ticketbooking.ui.common.UiState
import com.example.ticketbooking.ui.favorites.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        bindObservers()
        viewModel.loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        // Làm mới danh sách khi quay lại màn hình
        viewModel.loadFavorites()
    }

    private fun setupUi() {
        binding.backBtn.setOnClickListener { finish() }
        binding.recyclerViewFavorites.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.emptyTxt.setOnClickListener { finish() } // simple CTA: quay lại khám phá
    }

    private fun bindObservers() {
        viewModel.favorites.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarFavorites.visibility = View.GONE
                    binding.emptyTxt.visibility = View.GONE
                    binding.recyclerViewFavorites.adapter = SkeletonFilmAdapter(6)
                }
                is UiState.Success -> {
                    binding.progressBarFavorites.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.emptyTxt.visibility = View.VISIBLE
                        binding.recyclerViewFavorites.adapter = null
                    } else {
                        binding.emptyTxt.visibility = View.GONE
                        val adapter = FavoritesAdapter(onToggleFavorite = { film ->
                            viewModel.toggleFavorite(film)
                        })
                        binding.recyclerViewFavorites.adapter = adapter
                        adapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.progressBarFavorites.visibility = View.GONE
                    binding.emptyTxt.visibility = View.VISIBLE
                }
            }
        }
    }
}