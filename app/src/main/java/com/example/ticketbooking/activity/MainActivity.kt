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
import com.example.ticketbooking.common.UserPrefs
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.content.Intent
import com.example.ticketbooking.common.IntentKeys

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
        updateGreeting()
        bindObservers()
        viewModel.loadBanners()
        viewModel.loadTopMovies()
        viewModel.loadUpcoming()

        // Điều hướng bottom menu
        setupBottomNavigation()

        // Tìm kiếm: mở trang SearchActivity khi người dùng bấm Search
        binding.editTextText4.setOnEditorActionListener { v, actionId, event ->
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            val isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            if (isSearch || isEnter) {
                val query = v.text?.toString()?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra(IntentKeys.QUERY, query)
                    startActivity(intent)
                }
                true
            } else {
                false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
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
                    // Chỉ hiển thị 4 phim ban đầu
                    binding.recyclerViewTopMovie.adapter = FilmListAdapter(state.data.take(4).toMutableList())
                    // Xử lý nút Xem tất cả cho Top phim
                    binding.seeallTopMovies.setOnClickListener {
                        val intent = Intent(this, SeeAllActivity::class.java)
                        intent.putExtra(IntentKeys.CATEGORY, SeeAllActivity.CATEGORY_TOP)
                        startActivity(intent)
                    }
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
                    // Chỉ hiển thị 4 phim ban đầu
                    binding.recyclerViewUpcoming.adapter = FilmListAdapter(state.data.take(4).toMutableList())
                    // Xử lý nút Xem tất cả cho Sắp phát hành
                    binding.seeALlUpComing.setOnClickListener {
                        val intent = Intent(this, SeeAllActivity::class.java)
                        intent.putExtra(IntentKeys.CATEGORY, SeeAllActivity.CATEGORY_UPCOMING)
                        startActivity(intent)
                    }
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

    private fun setupBottomNavigation() {
        val bottom = findViewById<ChipNavigationBar>(com.example.ticketbooking.R.id.bottomNav)
        bottom.setOnItemSelectedListener { id ->
            when (id) {
                com.example.ticketbooking.R.id.favorites -> {
                    startActivity(android.content.Intent(this, FavoritesActivity::class.java))
                }
                com.example.ticketbooking.R.id.explorer -> {
                    // Đang ở màn Home, không cần làm gì
                }
                com.example.ticketbooking.R.id.cart -> {
                    startActivity(android.content.Intent(this, OrdersActivity::class.java))
                }
                com.example.ticketbooking.R.id.profile -> {
                    startActivity(android.content.Intent(this, ProfileActivity::class.java))
                }
            }
        }
    }

    private fun updateGreeting() {
        val name = UserPrefs.getName(this)
        val username = UserPrefs.getUsername(this)
        binding.textView.text = "Hello $name"
        binding.textView9.text = username
    }
}
