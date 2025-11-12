package com.example.ticketbooking.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.activity.DetailFilmActivity
import com.example.ticketbooking.common.IntentKeys
import com.example.ticketbooking.databinding.ViewholderFavoriteBinding
import com.example.ticketbooking.model.Film

class FavoritesAdapter(
    private val onToggleFavorite: (Film) -> Unit
) : ListAdapter<Film, FavoritesAdapter.ViewHolder>(Diff) {

    private var context: Context? = null

    inner class ViewHolder(private val binding: ViewholderFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Film) {
            binding.nameTxt.text = item.Title

            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(30))
            Glide.with(binding.root.context)
                .load(item.Poster)
                .apply(requestOptions)
                .into(binding.pic)

            // Favorites screen: items are favorited; show filled icon
            binding.favToggle.setImageResource(com.example.ticketbooking.R.drawable.ic_favorite_filled)

            binding.favToggle.setOnClickListener { onToggleFavorite(item) }

            binding.root.setOnClickListener {
                val intent = Intent(context, DetailFilmActivity::class.java)
                intent.putExtra(IntentKeys.OBJECT, item)
                context?.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object Diff : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            return (oldItem.Title ?: "") == (newItem.Title ?: "") && (oldItem.Year ?: "") == (newItem.Year ?: "")
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem == newItem
        }
    }
}