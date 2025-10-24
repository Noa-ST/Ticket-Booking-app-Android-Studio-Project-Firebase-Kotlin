package com.example.ticketbooking.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.DateAdapter
import com.example.ticketbooking.databinding.ActivitySeatListBinding
import com.example.ticketbooking.model.Film
import java.time.format.DateTimeFormatter
import java.time.LocalDate

class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var film: Film
    private var price: Double=0.0
    private var number: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getIntentExtra()
        setVariable()
        initTimeDatelist()
    }
    private fun initTimeDatelist() {
        binding.apply {
            LinearLayoutManager (this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            dateRecyclerview.adapter= DateAdapter(generateDates())
        }
    }
    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish()}
    }
    private fun getIntentExtra() {
        film=intent.getSerializableExtra("film") as Film
    }
    private fun generateDates(): List<String>{
        val dates=mutableListOf<String>()
        val today= LocalDate.now()
        val formatter= DateTimeFormatter.ofPattern("EEE/dd/MMM")

        for(i in 0 until 7){
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        return dates
    }
}
