package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.R
import com.example.ticketbooking.databinding.ItemSeatBinding
import com.example.ticketbooking.model.Seat

class SeatListAdapter(private var seatList: List<Seat>,
                      private val context: Context,
                      private val onSeatClick: (Int) -> Unit
) :
    RecyclerView.Adapter<SeatListAdapter.Viewholder>() {
    private val selectedSeatName = ArrayList<String>()
    inner class Viewholder(val binding: ItemSeatBinding) :
    RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeatListAdapter.Viewholder {
        return Viewholder(
            ItemSeatBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SeatListAdapter.Viewholder, position: Int) {
        val seat=seatList[position]
        holder.binding.seatTxt.text = seat.name
        when(seat.status) {
            Seat.SeatStatus.AVAILABLE -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_available)
                holder.binding.seatTxt.setTextColor(context.getColor(R.color.white))
            }
            Seat.SeatStatus.SELECTED -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_selected)
                holder.binding.seatTxt.setTextColor(context.getColor(R.color.black))
            }
            Seat.SeatStatus.UNAVAILABLE -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seatTxt.setTextColor(context.getColor(R.color.grey))
            }
        }
        holder.binding.seatTxt.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            // Chỉ cho phép tương tác khi ghế Available hoặc Selected (toggle)
            when (seatList[pos].status) {
                Seat.SeatStatus.AVAILABLE, Seat.SeatStatus.SELECTED -> onSeatClick(pos)
                Seat.SeatStatus.UNAVAILABLE -> { /* bỏ qua */ }
            }
        }
    }
    override fun getItemCount(): Int = seatList.size

    fun updateData(newList: List<Seat>) {
        seatList = newList
        notifyDataSetChanged()
    }
}