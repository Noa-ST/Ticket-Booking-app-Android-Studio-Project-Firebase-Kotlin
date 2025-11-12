package com.example.ticketbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.databinding.ViewholderFilmSkeletonBinding

class SkeletonFilmAdapter(private val count: Int = 6) : RecyclerView.Adapter<SkeletonFilmAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ViewholderFilmSkeletonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderFilmSkeletonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // No binding needed; static skeleton layout
    }

    override fun getItemCount(): Int = count
}