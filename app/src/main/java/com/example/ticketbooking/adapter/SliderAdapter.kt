package com.example.ticketbooking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.databinding.ViewholderSliderBinding
import com.example.ticketbooking.model.SliderItems

class SliderAdapter (
    private var sliderItems: MutableList<SliderItems>, private val viewPager2: ViewPager2
): RecyclerView.Adapter<SliderAdapter.SliderViewholder>() {
    private var context: Context? = null
    private val runnable = Runnable {
        sliderItems.addAll(sliderItems)
        notifyDataSetChanged()
    }
    inner class SliderViewholder (private val binding: ViewholderSliderBinding):
    RecyclerView.ViewHolder(binding.root) {
        fun bind(sliderItems: SliderItems) {
            context?.let {
                Glide.with(it)
                    .load(sliderItems.image)
                    .apply (
                        RequestOptions().transform(CenterCrop(), RoundedCorners(60))
                    )
                    .into(binding.imageSlide )
            }
        }
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdapter.SliderViewholder {
        context = parent.context
        val binding = ViewholderSliderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SliderViewholder(binding)
    }

    override fun onBindViewHolder(holder: SliderAdapter.SliderViewholder, position: Int) {
        holder.bind(sliderItems[position])
        if (position == sliderItems.size - 2) {
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int = sliderItems.size
}