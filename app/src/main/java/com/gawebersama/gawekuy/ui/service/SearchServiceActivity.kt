package com.gawebersama.gawekuy.ui.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.data.adapter.ServiceAdapter
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivitySearchServiceBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchServiceBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private lateinit var serviceAdapter: ServiceAdapter

    private var searchJob: Job? = null
    private var currentQuery: String? = null  // Menyimpan query yang sedang berlaku
    private var currentCategory: String? = null  // Menyimpan kategori yang sedang dipilih

    companion object {
        const val TAG = "SearchServiceActivity"
        const val CATEGORY = "category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window inset for edge-to-edge display
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
            val category = intent.getStringExtra(CATEGORY)

            btnBack.setOnClickListener { finish() }

            // Set up adapter and click listeners
            serviceAdapter = ServiceAdapter(
                onItemClick = { service ->
                    val intent = Intent(this@SearchServiceActivity, ServiceDetailActivity::class.java)
                    intent.putExtra(ServiceDetailActivity.SERVICE_ID, service.serviceId)
                    startActivity(intent)
                },
                onHoldClick = { }
            )

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(this@SearchServiceActivity)
                adapter = serviceAdapter
            }

            // Load more button, outside of search logic
            btnLoadMore.setOnClickListener {
                currentQuery?.let { query ->
                    Log.d(TAG, "Loading more for query: $query")
                    if (currentCategory != null) {
                        val filter = getCategoryFilter(category)
                        serviceViewModel.searchService(filter, query, resetPaging = false)
                        Log.d(TAG, "Category: $category, Query: $query")
                    } else {
                        serviceViewModel.searchService(null, query, resetPaging = false)
                    }
                }
            }

            // SearchView setup with debounce
            svServices.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        handleSearch(it, resetPaging = true)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchJob?.cancel()  // Cancel previous search job
                    searchJob = lifecycleScope.launch {
                        delay(500)  // Debounce delay
                        newText?.let { handleSearch(it, resetPaging = true) }
                    }
                    return true
                }
            })
        }
    }

    private fun handleSearch(query: String, resetPaging: Boolean = false) {
        val category = intent.getStringExtra(CATEGORY)
        currentQuery = query
        currentCategory = category

        val filter = getCategoryFilter(category)
        Log.d(TAG, "Category: $category, Query: $query")
        serviceViewModel.searchService(filter, query, resetPaging)
    }

    private fun getCategoryFilter(category: String?): FilterAndOrderService {
        return when (category) {
            "Penulisan & Akademik" -> FilterAndOrderService.ACADEMIC_CHEAP
            "Desain & Multimedia" -> FilterAndOrderService.DESIGN_CHEAP
            "Pemasaran & Media Sosial" -> FilterAndOrderService.MARKETING_CHEAP
            "Pengembangan Teknologi" -> FilterAndOrderService.TECH_CHEAP
            "Lainnya" -> FilterAndOrderService.OTHERS_CHEAP
            else -> FilterAndOrderService.RATING // Default filter if no category
        }
    }

    private fun observeViewModel() {
        serviceViewModel.serviceWithUser.observe(this@SearchServiceActivity) { services ->
            Log.d(TAG, "Fetched Services: $services")
            serviceAdapter.submitList(services)
            serviceAdapter.notifyDataSetChanged()
            binding.btnLoadMore.visibility = if (serviceViewModel.hasMoreData()) View.VISIBLE else View.GONE
        }
    }
}
