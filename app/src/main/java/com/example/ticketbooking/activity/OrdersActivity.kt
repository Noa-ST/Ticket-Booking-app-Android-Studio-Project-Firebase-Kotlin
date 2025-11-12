package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticketbooking.adapter.OrderListAdapter
import com.example.ticketbooking.databinding.ActivityOrdersBinding
import com.example.ticketbooking.model.Order
import com.example.ticketbooking.ui.common.UiState
import com.example.ticketbooking.ui.orders.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.widget.ArrayAdapter
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.RecyclerView

@AndroidEntryPoint
class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private val viewModel: OrdersViewModel by viewModels()
    private var allOrders: List<Order> = emptyList()
    private var currentFilter: String? = null
    private var currentQuery: String = ""
    private var sortMode: String = "DATE_DESC"
    private val pageSize: Int = 10
    private var currentPage: Int = 1
    private lateinit var orderAdapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        bindObservers()
        viewModel.loadOrders("guest")
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders("guest")
    }

    private fun setupUi() {
        binding.backBtn.setOnClickListener { finish() }
        binding.recyclerViewOrders.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Adapter khởi tạo rỗng cho phân trang
        orderAdapter = OrderListAdapter(mutableListOf(), { order -> onOrderClick(order) }, { order -> onCancelClick(order) })
        binding.recyclerViewOrders.adapter = orderAdapter

        // Bộ lọc trạng thái
        binding.filterAll.setOnClickListener { setFilter(null) }
        binding.filterHeld.setOnClickListener { setFilter("HELD") }
        binding.filterPaid.setOnClickListener { setFilter("PAID") }
        binding.filterCancelled.setOnClickListener { setFilter("CANCELLED") }
        binding.filterExpired.setOnClickListener { setFilter("EXPIRED") }

        // Tìm kiếm theo mã đơn / tên phim
        binding.searchInputOrders.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim() ?: ""
                refreshList(resetPage = true)
            }
        })

        // Spinner sắp xếp
        val options = listOf("Mới nhất", "Cũ nhất", "Giá ↑", "Giá ↓")
        binding.sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        binding.sortSpinner.setSelection(0)
        binding.sortSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                sortMode = when (position) {
                    0 -> "DATE_DESC"
                    1 -> "DATE_ASC"
                    2 -> "PRICE_ASC"
                    else -> "PRICE_DESC"
                }
                refreshList(resetPage = true)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // Phân trang vô hạn
        binding.recyclerViewOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = lm.findLastVisibleItemPosition()
                val total = orderAdapter.itemCount
                val data = applyFilterSortQuery(allOrders)
                if (dy > 0 && lastVisible >= total - 2 && total < data.size) {
                    loadNextPage(data)
                }
            }
        })
    }

    private fun bindObservers() {
        viewModel.orders.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarOrders.visibility = View.VISIBLE
                    binding.emptyTxt.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBarOrders.visibility = View.GONE
                    allOrders = state.data
                    refreshList(resetPage = true)
                }
                is UiState.Error -> {
                    binding.progressBarOrders.visibility = View.GONE
                    binding.emptyTxt.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setFilter(status: String?) {
        currentFilter = status
        // UI feedback đơn giản: đổi nền của nút Tất cả sang orange, còn lại light black
        fun select(v: android.widget.TextView, selected: Boolean) {
            v.setBackgroundResource(if (selected) com.example.ticketbooking.R.drawable.orange_bg else com.example.ticketbooking.R.drawable.light_black_bg)
            v.setTextColor(if (selected) getColor(com.example.ticketbooking.R.color.black) else getColor(com.example.ticketbooking.R.color.white))
        }
        select(binding.filterAll, status == null)
        select(binding.filterHeld, status == "HELD")
        select(binding.filterPaid, status == "PAID")
        select(binding.filterCancelled, status == "CANCELLED")
        select(binding.filterExpired, status == "EXPIRED")

        refreshList(resetPage = true)
    }

    private fun applyFilterSortQuery(src: List<Order>): List<Order> {
        // Filter theo trạng thái
        val filtered = currentFilter?.let { status -> src.filter { it.status == status } } ?: src
        // Tìm kiếm theo mã đơn hoặc tên phim từ showtimeId
        val query = currentQuery.lowercase()
        val searched = if (query.isBlank()) filtered else filtered.filter { o ->
            val codeHit = o.id.lowercase().contains(query)
            val titleHit = extractTitleFromShowtimeId(o.showtimeId).lowercase().contains(query)
            val showtimeHit = o.showtimeId.lowercase().contains(query)
            codeHit || titleHit || showtimeHit
        }
        // Sắp xếp
        val sorted = when (sortMode) {
            "DATE_ASC" -> searched.sortedBy { it.createdAt }
            "PRICE_ASC" -> searched.sortedBy { it.total }
            "PRICE_DESC" -> searched.sortedByDescending { it.total }
            else -> searched.sortedByDescending { it.createdAt }
        }
        return sorted
    }

    private fun refreshList(resetPage: Boolean) {
        val data = applyFilterSortQuery(allOrders)
        if (resetPage) currentPage = 1
        if (data.isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            orderAdapter.setItems(emptyList())
        } else {
            binding.emptyTxt.visibility = View.GONE
            val end = (currentPage * pageSize).coerceAtMost(data.size)
            orderAdapter.setItems(data.subList(0, end))
        }
    }

    private fun loadNextPage(fullData: List<Order>) {
        val totalLoaded = orderAdapter.itemCount
        val nextEnd = (totalLoaded + pageSize).coerceAtMost(fullData.size)
        if (nextEnd > totalLoaded) {
            val more = fullData.subList(totalLoaded, nextEnd)
            orderAdapter.appendItems(more)
            currentPage = (orderAdapter.itemCount + pageSize - 1) / pageSize
        }
    }

    private fun extractTitleFromShowtimeId(showtimeId: String): String {
        val parts = showtimeId.split("-")
        val slug = parts.dropLast(2).joinToString("-")
        return slug.replace("-", " ").split(" ").joinToString(" ") { s ->
            s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }.ifBlank { showtimeId }
    }

    private fun onOrderClick(order: Order) {
        if (order.status == "HELD") {
            // Mở lại CartActivity để tiếp tục thanh toán
            val cartItem = com.example.ticketbooking.model.CartItem(
                showtimeId = order.showtimeId,
                token = order.id, // token đã dùng làm id
                seatIndices = order.seatIndices,
                unitPrice = if (order.seatIndices.isNotEmpty()) order.base / order.seatIndices.size else 15.0,
                totalPrice = order.total,
                expiresAt = System.currentTimeMillis() + 5 * 60_000L
            )
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra(com.example.ticketbooking.common.IntentKeys.CART_ITEM, cartItem)
            startActivity(intent)
        }
        // Nếu PAID: sau này có thể mở chi tiết đơn hàng.
    }

    private fun onCancelClick(order: Order) {
        if (order.status != "HELD") return
        viewModel.cancelOrder(order) { ok ->
            runOnUiThread {
                if (ok) {
                    android.widget.Toast.makeText(this, "Đã hủy giữ ghế", android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.loadOrders("guest")
                } else {
                    android.widget.Toast.makeText(this, "Hủy đơn thất bại", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}