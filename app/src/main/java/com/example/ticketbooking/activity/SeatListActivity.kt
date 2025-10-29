package com.example.ticketbooking.activity

import android.icu.text.DecimalFormat
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.DateAdapter
import com.example.ticketbooking.adapter.SeatListAdapter
import com.example.ticketbooking.databinding.ActivitySeatListBinding
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.model.Seat
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
        initSeatsList()
    }

    private fun initSeatsList() {
        val gridLayoutManager= GridLayoutManager(this, 7)
        gridLayoutManager.spanSizeLookup=object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return if(position % 7 == 3) 1 else 1
            }
        }

        binding.apply {
            seatRecyclerview.layoutManager = gridLayoutManager

            val seatList=mutableListOf<Seat>()
            val numberSeats = 81
            for (i in 0 until numberSeats) {
                val seatName=""
                val seatStatus=
                    if(i==2 || i==20 || i==33 || i==41 || i==50 || i==72 || i==73) Seat.SeatStatus.UNAVAILABLE
                    else Seat.SeatStatus.AVAILABLE
                seatList.add(Seat(seatStatus, seatName))
            }

            val seatAdapter= SeatListAdapter(seatList, this@SeatListActivity, object : SeatListAdapter.SelectedSeat {
                override fun Return(selectedName: String, num: Int) {
                    numberSelectedTxt.text= "$num Seat Selected"
                    val df = DecimalFormat("#.##")
                    price = df.format(num * film.price).toDouble()
                    number = num
                    priceTxt.text = "$$price"
                }

            })
            seatRecyclerview.adapter = seatAdapter
            seatRecyclerview.isNestedScrollingEnabled = false
        }
    }

    private fun initTimeDatelist() {
        binding.apply {
            dateRecyclerview.layoutManager =
                LinearLayoutManager (this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            dateRecyclerview.adapter= DateAdapter(generateDates())

            timeRecyclerview.layoutManager =
                LinearLayoutManager (this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            timeRecyclerview.adapter= DateAdapter(generateTimeSlots())
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

    private fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

            for (i in 0 until 24 step 2){
                val time = LocalDate.now().atTime(i, 0)
                timeSlots.add(time.format(formatter))
            }
        return timeSlots
    }
}
