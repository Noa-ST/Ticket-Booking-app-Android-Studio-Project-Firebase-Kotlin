package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
// LỖI ĐÃ SỬA: Xóa import thừa này
// import com.example.ticketbooking.adapter.CastListAdapter.viewholder
import com.example.ticketbooking.databinding.ViewholderCastBinding
import com.example.ticketbooking.model.Cast

// LỖI ĐÃ SỬA: Đổi tên lớp ViewHolder thành Viewholder (viết hoa chữ V) để khớp với quy ước
// và để tránh xung đột với các khai báo annotation class trước đó.
class CastListAdapter(private val cast: ArrayList<Cast>):
// SỬA: SỬ DỤNG TÊN ĐÃ ĐỔI: CastListAdapter.Viewholder
    RecyclerView.Adapter<CastListAdapter.Viewholder>(){

    private var context: Context? = null

    // LỖI ĐÃ SỬA: Đổi tên lớp ViewHolder thành Viewholder (viết hoa chữ V)
    inner class Viewholder(private val binding: ViewholderCastBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(cast: Cast){
            context?.let{
                Glide.with(it)
                    .load(cast.Picurl)
                    .into(binding.actorimage)
            }
            binding.nameTxt.text = cast.Actor
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CastListAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // SỬA: Trả về đối tượng với tên lớp mới: Viewholder
        return Viewholder(binding)

    }

    override fun onBindViewHolder(
        holder: Viewholder, // SỬA: Dùng tên lớp mới: Viewholder
        position: Int
    ) {
        holder.bind(cast[position])
    }

    override fun getItemCount(): Int = cast.size

    // LỖI ĐÃ SỬA: XÓA hoàn toàn dòng này (dòng 52 cũ)
    // annotation class viewholder
}