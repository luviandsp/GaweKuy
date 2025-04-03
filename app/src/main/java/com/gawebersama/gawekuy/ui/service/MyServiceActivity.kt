package com.gawebersama.gawekuy.ui.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // Load data pertama kali
        serviceViewModel.fetchUserServices(resetPaging = true)
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
            fabCreateService.setOnClickListener {
                val intent = Intent(this@MyServiceActivity, CreateServiceActivity::class.java)
                launcher.launch(intent)
            }

            serviceAdapter = ServiceAdapter(
                onItemClick = { service ->
                    val intent = Intent(this@MyServiceActivity, ServiceDetailActivity::class.java)
                    intent.putExtra(ServiceDetailActivity.SERVICE_ID, service.serviceId)
                    startActivity(intent)
                },
                onHoldClick = { service ->
                    val intent = Intent(this@MyServiceActivity, CreateServiceActivity::class.java)
                    intent.putExtra(CreateServiceActivity.SERVICE_ID, service.serviceId)
                    startActivity(intent)
                }
            )

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(this@MyServiceActivity)
                adapter = serviceAdapter
            }

            rvFreelancer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    // Cek apakah masih ada data sebelum request lagi
                    if (lastVisibleItem >= totalItemCount - 3 && serviceViewModel.hasMoreData()) {
                        serviceViewModel.fetchUserServices()
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        serviceViewModel.serviceWithUser.observe(this) { services ->
            Log.d(TAG, "Fetched Services: $services")
            serviceAdapter.submitList(services)
            serviceAdapter.notifyDataSetChanged()

            if (services?.isEmpty() != false) {
                binding.llEmptyService.visibility = View.VISIBLE
            } else {
                binding.llEmptyService.visibility = View.GONE
            }

            binding.srlMyService.isRefreshing = false
        }
    }

    private fun refreshMyServiceData() {
        binding.srlMyService.isRefreshing = true
        serviceViewModel.fetchUserServices(resetPaging = true)

        lifecycleScope.launch {
            binding.srlMyService.isRefreshing = false
        }
    }
}