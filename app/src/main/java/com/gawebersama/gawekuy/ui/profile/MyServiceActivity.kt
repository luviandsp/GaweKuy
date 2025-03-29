package com.gawebersama.gawekuy.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.data.adapter.ServiceAdapter
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityMyServiceBinding

class MyServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyServiceBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private lateinit var serviceAdapter: ServiceAdapter

    companion object {
        const val TAG = "MyServiceActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyServiceBinding.inflate(layoutInflater)
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
            fabCreateService.setOnClickListener {
                val intent = Intent(this@MyServiceActivity, CreateServiceActivity::class.java)
                startActivity(intent)
            }

            // Inisialisasi Adapter dan RecyclerView
            serviceAdapter = ServiceAdapter { service ->
                val intent = Intent(this@MyServiceActivity, CreateServiceActivity::class.java).apply {
                    putExtra(CreateServiceActivity.SERVICE_ID, service.serviceId)
                }

                Log.d(TAG, "Clicked Service ID: ${service.serviceId}")

                startActivity(intent)
            }

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(this@MyServiceActivity)
                adapter = serviceAdapter
            }
        }
    }

    private fun observeViewModel() {
        serviceViewModel.services.observe(this) { services ->
            Log.d(TAG, "Fetched Services: $services") // Debug semua layanan
            if (services.isNotEmpty()) {
                Log.d(TAG, "First Service ID: ${services.first().serviceId}") // Debug service pertama
            }
            serviceAdapter.submitList(services)
        }
    }

    override fun onResume() {
        super.onResume()
        serviceViewModel.fetchUserServices() // Refresh data saat kembali ke activity ini
    }
}