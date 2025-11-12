package com.example.ticketbooking.adapter

import com.example.ticketbooking.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ticketbooking.databinding.ItemTimeBinding

class TimeAdapter(
    private val timeSlots: List<String>,
    private val onSelected: (Int) -> Unit
) :
    RecyclerView.Adapter<TimeAdapter.ViewHolder>() {
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class ViewHolder(private val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(time: String) {

                binding.TextViewTime.text = time

                if (selectedPosition == position) {
                    binding.TextViewTime.setBackgroundResource(R.drawable.yellow_bg)
                    binding.TextViewTime.setTextColor(binding.root.context.getColor(R.color.black))
                } else {
                    binding.TextViewTime.setBackgroundResource(R.drawable.light_black_bg)
                    binding.TextViewTime.setTextColor(binding.root.context.getColor(R.color.white))
                }
                binding.root.setOnClickListener {
                    val position = position
                    if (position != RecyclerView.NO_POSITION) {
                        lastSelectedPosition = selectedPosition
                        selectedPosition = position
                        notifyItemChanged(lastSelectedPosition)
                        notifyItemChanged(selectedPosition)
                        onSelected(position)
                    }
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeAdapter.ViewHolder {
        return ViewHolder(binding = ItemTimeBinding.inflate(
                LayoutInflater.from(parent.context),
                        parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeAdapter.ViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int =timeSlots.size
}