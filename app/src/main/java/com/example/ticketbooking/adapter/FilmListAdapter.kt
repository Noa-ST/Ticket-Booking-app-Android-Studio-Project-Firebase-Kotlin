package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.databinding.ViewholderFilmBinding
import com.example.ticketbooking.model.Film

class FilmListAdapter(private val items: ArrayList<Film>): RecyclerView.Adapter<FilmListAdapter.Viewhodler>() {
    private var context: Context? = null


    inner class Viewhodler(private var binding: ViewholderFilmBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Film){
            binding.nameTxt.text = item.Title
            var requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(30))

            Glide.with(context!!).load(item.Poster).apply(requestOptions).into(binding.pic)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilmListAdapter.Viewhodler {
        context=parent.context
        val  binding= ViewholderFilmBinding.inflate(LayoutInflater.from(context), parent, false)

        return  Viewhodler(binding)
    }

    override fun onBindViewHolder(holder: FilmListAdapter.Viewhodler, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}