package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.R
import com.example.ticketbooking.databinding.ItemSeatBinding
import com.example.ticketbooking.model.Seat

class SeatListAdapter(private val seatList:List<Seat>,
                      private val context: Context,
                      private val selectedSeat: SelectedSeat
    ):
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
            when(seat.status){
                Seat.SeatStatus.AVAILABLE -> {
                    seat.status= Seat.SeatStatus.SELECTED
                    selectedSeatName.add(seat.name)
                    notifyItemChanged(position)
                }
                Seat.SeatStatus.SELECTED -> {
                    seat.status= Seat.SeatStatus.AVAILABLE
                    selectedSeatName.add(seat.name)
                    notifyItemChanged(position)
                }
                else -> {}
            }
            val selected=selectedSeatName.joinToString(",")
            selectedSeat.Return(selected, selectedSeatName.size
            )
        }
    }

    override fun getItemCount(): Int = seatList.size

    interface SelectedSeat {
        fun Return(selectedName:String,num: Int)
    }
}