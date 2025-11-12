package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ticketbooking.R
import com.example.ticketbooking.adapter.CastListAdapter
import com.example.ticketbooking.adapter.GenreEachFilmAdapter
// Đảm bảo tên Binding khớp với tên file layout activity_detail_film.xml
import com.example.ticketbooking.databinding.ActivityDetailFilmBinding
import com.example.ticketbooking.model.Film
import com.example.ticketbooking.common.IntentKeys
import eightbitlab.com.blurview.RenderScriptBlur
// Cần import Cast
import com.example.ticketbooking.model.Cast
import com.example.ticketbooking.ui.detail.DetailFilmViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.ticketbooking.data.favorite.FavoritesRepository

@AndroidEntryPoint
class DetailFilmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFilmBinding
    private lateinit var viewModel: DetailFilmViewModel
    @Inject lateinit var favoritesRepository: FavoritesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // SỬA LỖI THỤT LỀ NHỎ
        binding = ActivityDetailFilmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[DetailFilmViewModel::class.java]
        bindObservers()
        setVariable()
    }

    private fun setVariable() {
        // Nhận dữ liệu từ Intent - an toàn theo API level (Parcelable)
        val film: Film? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(IntentKeys.OBJECT, Film::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(IntentKeys.OBJECT)
        }

        // Nếu film là null, không làm gì cả hoặc hiển thị lỗi
        if (film == null) {
            // Ví dụ: hiển thị Toast lỗi và đóng Activity
            // Toast.makeText(this, "Lỗi tải dữ liệu phim", Toast.LENGTH_SHORT).show()
            finish()
            return // Thoát khỏi hàm nếu film là null
        }

        viewModel.setFilm(film)

        // Bo góc dưới ảnh poster
        val requestOptions = RequestOptions()
            .transform(CenterCrop(),
                GranularRoundedCorners(0f, 0f, 50f, 50f))

        // Nút quay lại
        binding.backBtn.setOnClickListener { finish() }

        // Làm mờ background cho blueView (Giữ nguyên)
        val radius = 10f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        binding.blueView.setupWith(rootView, RenderScriptBlur(this))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)
        binding.blueView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blueView.clipToOutline = true

        // Các danh sách sẽ được bind qua observer trong bindObservers()

        // Nút mua vé
        binding.buyTicketBtn.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java)
            // Đã kiểm tra film != null ở trên
            intent.putExtra(IntentKeys.FILM, film)
            startActivity(intent)
        }

        // Nút đánh dấu yêu thích (bookmark)
        binding.bookmarkBtn.setOnClickListener {
            val currentFilm = viewModel.film.value ?: return@setOnClickListener
            val nowFav = favoritesRepository.toggleFavorite(currentFilm)
            applyFavoriteUi(nowFav)
        }
    }

    private fun bindObservers() {
        viewModel.film.observe(this) { film ->
            if (film == null) return@observe

            val requestOptions = RequestOptions()
                .transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 50f, 50f))

            Glide.with(this)
                .load(film.Poster)
                .apply(requestOptions)
                .into(binding.filmPic)

            binding.titleTxt.text = film.Title
            binding.imdbTxt.text = "IMDB ${film.Imdb}"
            binding.movieTimeTxt.text = "${film.Year} - ${film.Time}"
            binding.movieSummeryTxt.text = film.Description

            film.Genre?.let { genreList ->
                if (genreList.isNotEmpty()) {
                    binding.genreView.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    binding.genreView.adapter = GenreEachFilmAdapter(ArrayList(genreList))
                }
            }

            film.Casts?.let { castList ->
                if (castList.isNotEmpty()) {
                    binding.castListView.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    binding.castListView.adapter = CastListAdapter(castList)
                }
            }

            // Áp dụng trạng thái yêu thích ban đầu
            val isFav = favoritesRepository.isFavorite(film)
            applyFavoriteUi(isFav)
        }
    }

    private fun applyFavoriteUi(isFavorite: Boolean) {
        // Đổi màu biểu tượng để phản ánh trạng thái yêu thích
        val color = if (isFavorite) getColor(R.color.green) else getColor(R.color.white)
        binding.bookmarkBtn.setColorFilter(color)
        binding.bookmarkBtn.contentDescription = if (isFavorite) "Favorited" else "Bookmark"
    }
}