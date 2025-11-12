package com.example.ticketbooking.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ticketbooking.adapter.FilmListAdapter
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.databinding.ActivitySearchBinding
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.viewmodel.SearchViewModel

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val repository = FilmRepository()
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        val query = intent.getStringExtra(IntentKeys.QUERY)?.trim().orEmpty()
        binding.searchInput.setText(query)
        binding.searchInput.setSelection(query.length)

        // Tìm kiếm ngay khi bấm nút tìm trên bàn phím
        binding.searchInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.setQuery(v.text?.toString()?.trim().orElseEmpty())
                true
            } else false
        }

        // Live search với debounce
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim().orEmpty()
                scheduleDebouncedQuery(text)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // Observe dữ liệu và kết quả
        viewModel.allFilms.observe(this) { films ->
            setupSpinners(films)
        }
        viewModel.results.observe(this) { results ->
            showResults(results, binding.searchInput.text?.toString()?.trim().orEmpty())
        }

        // Tải dữ liệu ban đầu rồi hiển thị theo query
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadAllFilms(repository) {
            binding.progressBar.visibility = View.GONE
            viewModel.setQuery(query)
        }

        // Nút bộ lọc nâng cao (multi-select + khoảng năm)
        binding.advancedFilterBtn?.setOnClickListener {
            openAdvancedFilterDialog()
        }
    }

    private fun scheduleDebouncedQuery(query: String) {
        searchRunnable?.let { debounceHandler.removeCallbacks(it) }
        searchRunnable = Runnable { viewModel.setQuery(query) }
        debounceHandler.postDelayed(searchRunnable!!, 350)
    }

    private fun uniqueFilms(list: List<Film>): List<Film> {
        // Khóa duy nhất ưu tiên Title + Year; fallback Poster nếu Title rỗng
        return list.distinctBy { film ->
            val titleKey = (film.Title ?: "").lowercase().trim()
            val yearKey = film.Year
            val posterKey = (film.Poster ?: "").lowercase().trim()
            if (titleKey.isNotEmpty()) "${titleKey}|${yearKey}" else "poster:${posterKey}"
        }
    }

    private fun setupSpinners(allFilms: List<Film>) {
        // Genres
        val genreSet = allFilms
            .flatMap { (it.Genre ?: arrayListOf<String>()).map { g -> g.trim() } }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
            .toSet()
            .toList()
            .sorted()
        val genreOptions = listOf("Tất cả thể loại") + genreSet.map { it.replaceFirstChar { c -> c.uppercase() } }
        val genreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genreOptions)
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genreSpinner.adapter = genreAdapter
        binding.genreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val genre = if (position == 0) null else genreOptions[position]
                val current = viewModel.selectedGenres.value ?: emptySet()
                val newSet = if (genre == null) emptySet() else setOf(genre)
                // Spinner giữ chọn một thể loại; advanced dialog sẽ cho multi-select
                viewModel.setGenres(newSet)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Years
        val years = allFilms.map { it.Year }.distinct().sortedDescending()
        val yearOptionsDisplay = listOf("Tất cả năm") + years.map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearOptionsDisplay)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearSpinner.adapter = yearAdapter
        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedYear = if (position == 0) null else years[position - 1]
                viewModel.setYearRange(selectedYear, selectedYear)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showResults(results: List<Film>, query: String) {
        binding.progressBar.visibility = View.GONE
        binding.resultCountTxt.text = "${results.size} kết quả"
        binding.resultCountTxt.visibility = View.VISIBLE
        if (results.isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.emptyTxt.text = "Không tìm thấy phim cho: \"$query\""
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerView.adapter = FilmListAdapter(results)
        }
    }

    // tiện ích nhỏ để tránh null
    private fun CharSequence?.orElseEmpty(): String = this?.toString() ?: ""

    private fun openAdvancedFilterDialog() {
        val ctx = this
        val dialogView = layoutInflater.inflate(com.example.ticketbooking.R.layout.dialog_filters, null)
        val genreListView = dialogView.findViewById<android.widget.ListView>(com.example.ticketbooking.R.id.genreListView)
        val startSpinner = dialogView.findViewById<android.widget.Spinner>(com.example.ticketbooking.R.id.startYearSpinner)
        val endSpinner = dialogView.findViewById<android.widget.Spinner>(com.example.ticketbooking.R.id.endYearSpinner)

        val films = viewModel.allFilms.value ?: emptyList()
        val genres = films
            .flatMap { (it.Genre ?: arrayListOf<String>()).map { g -> g.trim() } }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
            .distinct()
            .sorted()
        val years = films.map { it.Year }.distinct().sortedDescending()

        // Genre multi-choice
        genreListView.choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE
        val genreDisplay = genres.map { it.replaceFirstChar { c -> c.uppercase() } }
        genreListView.adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_multiple_choice, genreDisplay)
        // Preselect
        val selected = viewModel.selectedGenres.value ?: emptySet()
        genreDisplay.forEachIndexed { idx, g ->
            if (selected.map { it.lowercase() }.contains(genres[idx])) {
                genreListView.setItemChecked(idx, true)
            }
        }

        // Year range spinners
        val yearDisplay = listOf("Tất cả") + years.map { it.toString() }
        val startAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, yearDisplay)
        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        startSpinner.adapter = startAdapter
        val endAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, yearDisplay)
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        endSpinner.adapter = endAdapter

        // Preselect year range
        val start = viewModel.yearStart.value
        val end = viewModel.yearEnd.value
        startSpinner.setSelection(start?.let { years.indexOf(it) + 1 } ?: 0)
        endSpinner.setSelection(end?.let { years.indexOf(it) + 1 } ?: 0)

        androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("Bộ lọc nâng cao")
            .setView(dialogView)
            .setPositiveButton("Áp dụng") { d, _ ->
                val selectedGenres = mutableSetOf<String>()
                for (i in 0 until genreListView.count) {
                    if (genreListView.isItemChecked(i)) selectedGenres.add(genreDisplay[i])
                }
                val startSel = startSpinner.selectedItemPosition.takeIf { it > 0 }?.let { years[it - 1] }
                val endSel = endSpinner.selectedItemPosition.takeIf { it > 0 }?.let { years[it - 1] }
                // Đảm bảo khoảng hợp lệ nếu cả hai được chọn
                val (finalStart, finalEnd) = if (startSel != null && endSel != null && startSel > endSel) {
                    // đổi chỗ
                    endSel to startSel
                } else {
                    startSel to endSel
                }
                viewModel.setGenres(selectedGenres)
                viewModel.setYearRange(finalStart, finalEnd)
                d.dismiss()
            }
            .setNegativeButton("Hủy") { d, _ -> d.dismiss() }
            .show()
    }
}