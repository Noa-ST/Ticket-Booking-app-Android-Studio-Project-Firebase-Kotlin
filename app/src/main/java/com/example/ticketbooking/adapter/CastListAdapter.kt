package com.example.ticketbooking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ticketbooking.R
import com.example.ticketbooking.model.Cast

class CastListAdapter(
    private val castList: List<Cast>
) : RecyclerView.Adapter<CastListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val castImage: ImageView = itemView.findViewById(R.id.castImage)
        val castName: TextView = itemView.findViewById(R.id.castName)
        val castRole: TextView = itemView.findViewById(R.id.castRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cast = castList[position]

        holder.castName.text = cast.name
        holder.castRole.text = cast.role

        Glide.with(holder.itemView.context)
            .load(cast.image)
            .placeholder(R.drawable.default_cast) // bạn có thể dùng hình mặc định
            .into(holder.castImage)
    }

    override fun getItemCount(): Int = castList.size
}
