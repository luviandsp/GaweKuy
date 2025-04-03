package com.gawebersama.gawekuy.ui.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.ServiceAdapter
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityServiceSearchCategoryBinding
import kotlinx.coroutines.launch

class ServiceSearchCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceSearchCategoryBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private lateinit var serviceAdapter: ServiceAdapter

    private var isExpensive : Boolean = false

    companion object {
        const val TAG = "ServiceSearchCategory"
        const val CATEGORY = "category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceSearchCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            srlSearchCategory.setOnRefreshListener {
                refreshCategoryData()
            }

            btnExpensive.setTextColor(getColor(R.color.inactive_color_text))
            btnExpensive.setBackgroundColor(getColor(R.color.inactive_color))
            btnExpensive.setStrokeColorResource(R.color.inactive_color)
        }

        initViews()
        observeViewModel()
        getServiceCheap()
    }

    private fun initViews() {
        with(binding) {
            val category = intent.getStringExtra(CATEGORY)

            tvTitle.text = category ?: getString(R.string.service_category)
            loadCategoryDesc()

            btnBack.setOnClickListener { finish() }

            btnLoadMore.setOnClickListener {
                if (isExpensive == false) {
                    loadServiceCheap()
                } else {
                    loadServiceExpensive()
                }
            }

            sbSearchService.setOnClickListener {
                val intent = Intent(this@ServiceSearchCategoryActivity, SearchServiceActivity::class.java)
                intent.putExtra(SearchServiceActivity.CATEGORY, category)
                startActivity(intent)
            }

            btnCheap.setOnClickListener {
                getServiceCheap()
                isExpensive = false

                btnCheap.setTextColor(getColor(R.color.blue))
                btnCheap.setBackgroundColor(getColor(R.color.white))
                btnCheap.setStrokeColorResource(R.color.blue)

                btnExpensive.setTextColor(getColor(R.color.inactive_color_text))
                btnExpensive.setBackgroundColor(getColor(R.color.inactive_color))
                btnExpensive.setStrokeColorResource(R.color.inactive_color)
            }

            btnExpensive.setOnClickListener {
                getServiceExpensive()
                isExpensive = true

                btnExpensive.setTextColor(getColor(R.color.blue))
                btnExpensive.setBackgroundColor(getColor(R.color.white))
                btnExpensive.setStrokeColorResource(R.color.blue)

                btnCheap.setTextColor(getColor(R.color.inactive_color_text))
                btnCheap.setBackgroundColor(getColor(R.color.inactive_color))
                btnCheap.setStrokeColorResource(R.color.inactive_color)
            }

            serviceAdapter = ServiceAdapter(
                onItemClick = { service ->
                    val intent = Intent(this@ServiceSearchCategoryActivity, ServiceDetailActivity::class.java)
                    intent.putExtra(ServiceDetailActivity.SERVICE_ID, service.serviceId)
                    startActivity(intent)
                },
                onHoldClick = { }
            )

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(this@ServiceSearchCategoryActivity)
                adapter = serviceAdapter
            }
        }
    }

    private fun getServiceCheap() {
        val category = intent.getStringExtra(CATEGORY)

        when (category) {
            "Penulisan & Akademik" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.ACADEMIC_CHEAP, resetPaging = true) }
            "Desain & Multimedia" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.DESIGN_CHEAP, resetPaging = true) }
            "Pemasaran & Media Sosial" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.MARKETING_CHEAP, resetPaging = true) }
            "Pengembangan Teknologi" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.TECH_CHEAP, resetPaging = true) }
            "Lainnya" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.OTHERS_CHEAP, resetPaging = true) }
        }
    }

    private fun getServiceExpensive() {
        val category = intent.getStringExtra(CATEGORY)

        when (category) {
            "Penulisan & Akademik" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.ACADEMIC_EXPENSIVE, resetPaging = true) }
            "Desain & Multimedia" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.DESIGN_EXPENSIVE, resetPaging = true) }
            "Pemasaran & Media Sosial" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.MARKETING_EXPENSIVE, resetPaging = true) }
            "Pengembangan Teknologi" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.TECH_EXPENSIVE, resetPaging = true) }
            "Lainnya" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.OTHERS_EXPENSIVE, resetPaging = true) }
        }
    }

    private fun loadServiceCheap() {
        val category = intent.getStringExtra(CATEGORY)

        when (category) {
            "Penulisan & Akademik" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.ACADEMIC_CHEAP) }
            "Desain & Multimedia" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.DESIGN_CHEAP) }
            "Pemasaran & Media Sosial" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.MARKETING_CHEAP) }
            "Pengembangan Teknologi" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.TECH_CHEAP) }
            "Lainnya" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.OTHERS_CHEAP) }
        }
    }

    private fun loadServiceExpensive() {
        val category = intent.getStringExtra(CATEGORY)

        when (category) {
            "Penulisan & Akademik" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.ACADEMIC_EXPENSIVE) }
            "Desain & Multimedia" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.DESIGN_EXPENSIVE) }
            "Pemasaran & Media Sosial" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.MARKETING_EXPENSIVE) }
            "Pengembangan Teknologi" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.TECH_EXPENSIVE) }
            "Lainnya" -> { serviceViewModel.fetchAllServices(FilterAndOrderService.OTHERS_EXPENSIVE) }
        }
    }

    private fun loadCategoryDesc() {
        val category = intent.getStringExtra(CATEGORY)

        when (category) {
            "Penulisan & Akademik" -> { binding.tvDesc.text = getString(R.string.academic_desc) }
            "Desain & Multimedia" -> { binding.tvDesc.text = getString(R.string.design_desc) }
            "Pemasaran & Media Sosial" -> { binding.tvDesc.text = getString(R.string.marketing_desc) }
            "Pengembangan Teknologi" -> { binding.tvDesc.text = getString(R.string.tech_desc) }
            "Lainnya" -> { binding.tvDesc.text = getString(R.string.others_desc) }
        }
    }

    private fun observeViewModel() {
        serviceViewModel.serviceWithUser.observe(this@ServiceSearchCategoryActivity) { services ->
            Log.d(TAG, "Fetched Services: $services")
            serviceAdapter.submitList(services)
            serviceAdapter.notifyDataSetChanged()  // Mencegah flickering
            binding.btnLoadMore.visibility = if (serviceViewModel.hasMoreData()) View.VISIBLE else View.GONE
            binding.srlSearchCategory.isRefreshing = false
        }
    }

    private fun refreshCategoryData() {
        binding.srlSearchCategory.isRefreshing = true

        if(isExpensive == false) {
            getServiceCheap()
        } else {
            getServiceExpensive()
        }

        lifecycleScope.launch {
            binding.srlSearchCategory.isRefreshing = false
        }
    }
}