package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.databinding.ItemOrderBinding
import com.example.ticketbooking.model.Order
import com.bumptech.glide.Glide
import com.example.ticketbooking.data.FilmRepository

class OrderListAdapter(
    private val items: MutableList<Order>,
    private val onClick: (Order) -> Unit,
    private val onCancel: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {
    private var context: Context? = null
    private val postersBySlug: MutableMap<String, String> = mutableMapOf()
    private var postersLoaded: Boolean = false

    private fun toSlug(title: String): String {
        return title
            .lowercase()
            .replace("&", "and")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun ensurePostersLoaded() {
        if (postersLoaded) return
        val repo = FilmRepository()
        // Load Top Movies
        repo.getTopMovies(
            onSuccess = { films ->
                films.forEach { f ->
                    val t = f.Title ?: return@forEach
                    val p = f.Poster ?: return@forEach
                    postersBySlug[toSlug(t)] = p
                }
                // Also load upcoming and then refresh
                repo.getUpcoming(
                    onSuccess = { upcoming ->
                        upcoming.forEach { f ->
                            val t = f.Title ?: return@forEach
                            val p = f.Poster ?: return@forEach
                            postersBySlug[toSlug(t)] = p
                        }
                        postersLoaded = true
                        notifyDataSetChanged()
                    },
                    onError = { postersLoaded = true }
                )
            },
            onError = { postersLoaded = true }
        )
    }

    inner class ViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Order) {
            // Parse showtimeId: <film-slug>-yyyyMMdd-HHmm
            val parts = item.showtimeId.split("-")
            val dt = parts.takeLast(2)
            val slug = parts.dropLast(2).joinToString("-")
            val filmTitle = slug.replace("-", " ").split(" ").joinToString(" ") { s ->
                s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }.ifBlank { item.showtimeId }

            val dateLabel = runCatching {
                val ymd = dt.getOrNull(0) ?: ""
                val yyyy = ymd.substring(0, 4).toInt()
                val mm = ymd.substring(4, 6).toInt()
                val dd = ymd.substring(6, 8).toInt()
                java.time.LocalDate.of(yyyy, mm, dd).format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM"))
            }.getOrElse { "" }
            val timeLabel = runCatching {
                val hm = dt.getOrNull(1) ?: ""
                val hh = hm.substring(0, 2).toInt()
                val mi = hm.substring(2, 4).toInt()
                java.time.LocalTime.of(hh, mi).format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
            }.getOrElse { "" }

            binding.tvFilmTitle.text = filmTitle
            binding.tvOrderId.text = "Mã đơn: ${item.id}"
            binding.tvShowDate.text = dateLabel
            binding.tvShowTime.text = timeLabel
            binding.tvStatus.text = item.status
            binding.tvTotal.text = String.format("$%.2f", item.total)
            binding.tvSeats.text = item.seatIndices.joinToString(", ")

            // Poster image
            val posterUrl = postersBySlug[slug]
            if (posterUrl != null) {
                Glide.with(binding.root).load(posterUrl).into(binding.imgPoster)
            } else {
                binding.imgPoster.setImageResource(com.example.ticketbooking.R.drawable.cinema)
            }

            // Cancel button only for HELD orders
            if (item.status == "HELD") {
                binding.btnCancel.visibility = android.view.View.VISIBLE
                binding.btnCancel.setOnClickListener { onCancel?.invoke(item) }
            } else {
                binding.btnCancel.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        ensurePostersLoaded()
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Order>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun appendItems(moreItems: List<Order>) {
        val start = items.size
        items.addAll(moreItems)
        notifyItemRangeInserted(start, moreItems.size)
    }
}