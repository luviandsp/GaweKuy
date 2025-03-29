package com.gawebersama.gawekuy.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gawebersama.gawekuy.databinding.ActivityFavoriteFreelancerBinding

class FavoriteFreelancerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteFreelancerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteFreelancerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }
        }
    }
}