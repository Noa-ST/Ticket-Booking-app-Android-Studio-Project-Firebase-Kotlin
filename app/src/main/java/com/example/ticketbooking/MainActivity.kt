package com.example.ticketbooking

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.ticketbooking.adapter.FilmListAdapter
import com.example.ticketbooking.adapter.SliderAdapter
import com.example.ticketbooking.databinding.ActivityMainBinding
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.model.SliderItems
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private var sliderHandler = Handler()
    private var sliderRunnable = Runnable{
        binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        initBanner()
        initTopMovies()
        initUpcomming()
        }

    private fun initTopMovies() {
        val myRef: DatabaseReference = database.getReference("Items")
        binding.progressBarTopMovie.visibility= View.VISIBLE
        var items= ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (i in snapshot.children){
                        val list = i.getValue(Film::class.java)
                        if(list != null){
                            items.add(list)
                        }
                    }
            if (items.isNotEmpty()){
                binding.recyclerViewTopMovie.layoutManager=
                    LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                binding.recyclerViewTopMovie.adapter =
                    FilmListAdapter(
                        items
                    )
            }
            binding.progressBarTopMovie.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun initBanner(){
        val myRef = database.getReference("Banners")
        binding.progressBarSlider.visibility = View.VISIBLE

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderItems>()
                for (i in snapshot.children){
                    val list = i.getValue(SliderItems::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                binding.progressBarSlider.visibility=View.GONE
                banners(lists)
        }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
    } )
    }

    private fun banners(list: MutableList<SliderItems>){
        binding.viewPager2.adapter = SliderAdapter(list, binding.viewPager2)
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem=1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)
            }
        })
    }

    private fun initUpcomming() {
        val myRef: DatabaseReference = database.getReference("Upcomming")
        binding.progressBarUpcoming.visibility= View.VISIBLE
        var items= ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (i in snapshot.children){
                        val list = i.getValue(Film::class.java)
                        if(list != null){
                            items.add(list)
                        }
                    }
                    if (items.isNotEmpty()){
                        binding.recyclerViewUpcoming.layoutManager=
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        binding.recyclerViewUpcoming.adapter =
                            FilmListAdapter(
                                items
                            )
                    }
                    binding.progressBarUpcoming.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    }
