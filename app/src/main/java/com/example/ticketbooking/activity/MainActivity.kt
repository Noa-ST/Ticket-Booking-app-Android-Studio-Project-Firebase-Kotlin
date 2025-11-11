package com.example.ticketbooking.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.ticketbooking.adapter.FilmListAdapter
import com.example.ticketbooking.adapter.SliderAdapter
import com.example.ticketbooking.databinding.ActivityMainBinding
import com.example.ticketbooking.ui.common.UiState
import com.example.ticketbooking.ui.main.MainViewModel
import com.example.ticketbooking.model.SliderItems
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var sliderHandler = Handler()
    private var sliderRunnable = Runnable {
        binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        bindObservers()
        viewModel.loadBanners()
        viewModel.loadTopMovies()
        viewModel.loadUpcoming()

    }

    private fun bindObservers() {
        viewModel.banners.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBarSlider.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBarSlider.visibility = View.GONE
                    banners(state.data.toMutableList())
                }
                is UiState.Error -> {
                    binding.progressBarSlider.visibility = View.GONE
                }
            }
        }

        viewModel.topMovies.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBarTopMovie.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBarTopMovie.visibility = View.GONE
                    binding.recyclerViewTopMovie.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerViewTopMovie.adapter = FilmListAdapter(state.data)
                }
                is UiState.Error -> {
                    binding.progressBarTopMovie.visibility = View.GONE
                }
            }
        }

        viewModel.upcoming.observe(this) { state ->
            when (state) {
                is UiState.Loading -> binding.progressBarUpcoming.visibility = View.VISIBLE
                is UiState.Success -> {
                    binding.progressBarUpcoming.visibility = View.GONE
                    binding.recyclerViewUpcoming.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerViewUpcoming.adapter = FilmListAdapter(state.data)
                }
                is UiState.Error -> {
                    binding.progressBarUpcoming.visibility = View.GONE
                }
            }
        }
    }

    private fun banners(list: MutableList<SliderItems>) {
        binding.viewPager2.adapter = SliderAdapter(list, binding.viewPager2)
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem = 1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                //sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })
    }

    // banners() giữ nguyên
}
