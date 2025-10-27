package com.example.ticketbooking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.R

class GenreEachFilmAdapter(
    private val genreList: List<String>
) : RecyclerView.Adapter<GenreEachFilmAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreTxt: TextView = itemView.findViewById(R.id.genreTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = genreList[position]
        holder.genreTxt.text = genre
    }

    override fun getItemCount(): Int = genreList.size
}
