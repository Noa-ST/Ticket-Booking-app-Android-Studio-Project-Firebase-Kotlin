package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ticketbooking.adapter.CastListAdapter.viewholder
import com.example.ticketbooking.databinding.ViewholderCastBinding
import com.example.ticketbooking.model.Cast

class CastListAdapter(private val cast: ArrayList<Cast>):
RecyclerView.Adapter<viewholder>(){

    private var context: Context? = null
    inner class viewholder(private val binding: ViewholderCastBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(cast: Cast){
        context?.let{
            Glide.with(it)
                .load(cast.Picurl)
                .into(binding.actorImage)


            }
            binding.nameTxt.text = cast.Actor

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CastListAdapter.viewholder {
        context = parent.context
        val binding = ViewholderCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewholder(binding)

    }

    override fun onBindViewHolder(
        holder: viewholder,
        position: Int
    ) {
        holder.bind(cast[position])
    }

    override fun getItemCount(): Int = cast.size

    annotation class viewholder
}
