package com.gawebersama.gawekuy.ui.portfolio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.gawebersama.gawekuy.data.adapter.PortfolioAdapter
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityFreelancerPortfolioBinding

class FreelancerPortfolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFreelancerPortfolioBinding
    private val servicePortfolioViewModel by viewModels<ServiceViewModel>()

    private lateinit var portfolioAdapter: PortfolioAdapter

    companion object {
        const val TAG = "FreelancerPortfolioActivity"
        const val SERVICE_ID = "service_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFreelancerPortfolioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serviceId = intent.getStringExtra(SERVICE_ID)
        Log.d(TAG, "Service ID received: $serviceId")

        if (serviceId != null) {
            servicePortfolioViewModel.fetchPortfolioByServiceId(serviceId)
        } else {
            finish()
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

            portfolioAdapter = PortfolioAdapter(
                onItemClick = { portfolio ->
                    val intent = Intent(this@FreelancerPortfolioActivity, PortfolioDetailActivity::class.java)
                    intent.putExtra(PortfolioDetailActivity.PORTFOLIO_ID, portfolio.portfolioId)
                    startActivity(intent)
                },
                onHoldClick = { }
            )

            rvPortfolio.apply {
                layoutManager = GridLayoutManager(this@FreelancerPortfolioActivity, 2)
                adapter = portfolioAdapter
            }
        }
    }

    private fun observeViewModel() {
        servicePortfolioViewModel.apply {
            selectedPortfolio.observe(this@FreelancerPortfolioActivity) { newPortfolioList ->
                portfolioAdapter.submitList(newPortfolioList)
                portfolioAdapter.notifyDataSetChanged()
                Log.d(TAG, "Portfolio List Updated: $newPortfolioList")

                if (newPortfolioList.isEmpty()) {
                    binding.llEmptyPortfolio.visibility = View.VISIBLE
                } else {
                    binding.llEmptyPortfolio.visibility = View.GONE
                }
            }
        }
    }
}