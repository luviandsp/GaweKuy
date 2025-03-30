package com.gawebersama.gawekuy.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.data.adapter.ServiceAdapter
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityMyServiceBinding
import kotlinx.coroutines.launch

class MyServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyServiceBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private lateinit var serviceAdapter: ServiceAdapter

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshMyServiceData()
        }
    }

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

        binding.srlMyService.setOnRefreshListener {
            refreshMyServiceData()
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
            fabCreateService.setOnClickListener {
                val intent = Intent(this@MyServiceActivity, CreateServiceActivity::class.java)
                launcher.launch(intent)
            }

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
        serviceViewModel.serviceWithUser.observe(this) { services ->
            Log.d(TAG, "Fetched Services: $services")
            serviceAdapter.submitList(services)
            binding.srlMyService.isRefreshing = false
        }
    }

    private fun refreshMyServiceData() {
        binding.srlMyService.isRefreshing = true

        serviceViewModel.fetchUserServices()

        lifecycleScope.launch {
            binding.srlMyService.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMyServiceData()
    }
}