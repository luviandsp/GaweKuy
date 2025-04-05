package com.gawebersama.gawekuy.ui.service

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gawebersama.gawekuy.databinding.ActivityOrderServiceBinding

class OrderServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderServiceBinding

    companion object {
        const val TAG = "OrderServiceActivity"
        const val SERVICE_ID = "service_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
        }
    }


    private fun observeViewModel() {

    }
}