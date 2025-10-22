package com.example.ticketbooking.activity

import android.os.Bundle
import android.renderscript.RenderScript
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.graphics.shapes.RoundedPolygon
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.CastListAdapter
import com.example.ticketbooking.adapter.GenreEachFilmAdapter
import com.example.ticketbooking.databinding.ActivityDetailFilm2Binding
import com.example.ticketbooking.model.Cast
import com.example.ticketbooking.model.Film
import eightbitlab.com.blurview.RenderScriptBlur

class DetailFilmActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFilm2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    binding= ActivityDetailFilm2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        setVariable()
        }

    private fun setVariable() {
        val film = intent.getSerializableExtra("object") as Film
        val requestOption = RequestOptions().transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 50f, 50f))

        Glide.with(this)
            .load(film.Poster)
            .apply ( requestOption )
            .into(binding.filmPic)

        binding.titleTxT.text = film.Title
        binding.imdbTxt.text = "IMDB ${film.Imdb}"
        binding.movieTimeTxt.text = "${film.Year} - ${film.Time}"
        binding.moviesummeryTxt.text = film.Description

        binding.backBtn.setOnClickListener {
            finish()
        }

        val radius = 10f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowsBackground = decorView.background

        binding.blueView.setupWith(rootView, RenderScriptBlur(  this))
            .setFrameClearDrawable(windowsBackground)
            .setBlurRadius(radius)
        binding.blueView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blueView.clipToOutline = true

        film.Genre?.let {
            binding.genreView.adapter = GenreEachFilmAdapter(it)
            binding.genreView.layoutManager=
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false )

        }

        film.Costs?.let {
            binding.castlistView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.castlistView.adapter = CastListAdapter(it)

        }
    }
}
