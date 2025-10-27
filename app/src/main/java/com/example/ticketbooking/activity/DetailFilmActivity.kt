package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.CastListAdapter
import com.example.ticketbooking.adapter.GenreEachFilmAdapter
import com.example.ticketbooking.databinding.ActivityDetailFilmBinding
import com.example.ticketbooking.model.Film
import eightbitlab.com.blurview.RenderScriptBlur

class DetailFilmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailFilmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailFilmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setVariable()
    }

    private fun setVariable() {
        // Nhận dữ liệu từ Intent
        val film = intent.getSerializableExtra("object") as? Film ?: return

        // Bo góc dưới ảnh poster
        val requestOptions = RequestOptions()
            .transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 50f, 50f))

        Glide.with(this)
            .load(film.Poster)
            .apply(requestOptions)
            .into(binding.filmPic)

        // Hiển thị thông tin phim
        binding.titleTxt.text = film.Title
        binding.imdbTxt.text = "IMDB ${film.Imdb}"
        binding.movieTimeTxt.text = "${film.Year} - ${film.Time}"
        binding.movieSummeryTxt.text = film.Description

        // Nút quay lại
        binding.backBtn.setOnClickListener { finish() }

        // Làm mờ background cho blueView
        val radius = 10f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        binding.blueView.setupWith(rootView, RenderScriptBlur(this))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)
        binding.blueView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blueView.clipToOutline = true

        // Adapter thể loại phim
        film.Genre?.let {
            binding.genreView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.genreView.adapter = GenreEachFilmAdapter(it)

        }

        // Adapter danh sách diễn viên
        film.Casts?.let {
            binding.castListView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.castListView.adapter = CastListAdapter(it)
        }

        // Nút mua vé
        binding.buyTicketBtn.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java)
            intent.putExtra("film", film)
            startActivity(intent)
        }
    }
}
