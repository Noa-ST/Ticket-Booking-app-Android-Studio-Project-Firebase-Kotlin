package com.example.ticketbooking.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ticketbooking.common.UserPrefs
import com.example.ticketbooking.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshProfileUi()

        binding.backBtn.setOnClickListener { finish() }

        binding.lnkOrders.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        binding.lnkFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnEdit.setOnClickListener { showEditDialog() }

        binding.btnLogout.setOnClickListener {
            UserPrefs.logout(this)
            refreshProfileUi()
            android.widget.Toast.makeText(this, "Đã đăng xuất về Guest", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshProfileUi()
    }

    private fun refreshProfileUi() {
        val name = UserPrefs.getName(this)
        val username = UserPrefs.getUsername(this)
        binding.tvName.text = name
        binding.tvEmail.text = username
    }

    private fun showEditDialog() {
        val view = LayoutInflater.from(this).inflate(com.example.ticketbooking.R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<android.widget.EditText>(com.example.ticketbooking.R.id.etName)
        val etUsername = view.findViewById<android.widget.EditText>(com.example.ticketbooking.R.id.etUsername)
        etName.setText(UserPrefs.getName(this))
        etUsername.setText(UserPrefs.getUsername(this))

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa hồ sơ")
            .setView(view)
            .setPositiveButton("Lưu") { dialog, _ ->
                val name = etName.text?.toString() ?: "Guest"
                val username = etUsername.text?.toString() ?: "guest"
                UserPrefs.setProfile(this, name, username)
                refreshProfileUi()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}