package com.example.ticketbooking.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.databinding.ViewholderGenreBinding

class GenreEachFilmAdapter(private val items:List<String>):
RecyclerView.Adapter<GenreEachFilmAdapter.viewholder>()
{
    class viewholder (val binding: ViewholderGenreBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GenreEachFilmAdapter.viewholder {
    val binding=
        ViewholderGenreBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return viewholder(binding)
    }

    override fun onBindViewHolder(holder: GenreEachFilmAdapter.viewholder, position: Int) {
        holder.binding.titleTxt.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}