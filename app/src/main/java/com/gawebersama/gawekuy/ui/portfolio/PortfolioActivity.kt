package com.gawebersama.gawekuy.ui.portfolio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.PortfolioAdapter
import com.gawebersama.gawekuy.data.viewmodel.PortfolioViewModel
import com.gawebersama.gawekuy.databinding.ActivityPortfolioBinding
import kotlinx.coroutines.launch

class PortfolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioBinding
    private val portfolioViewModel by viewModels<PortfolioViewModel>()

    private lateinit var portfolioAdapter: PortfolioAdapter

    companion object {
        const val TAG = "PortfolioActivity"
        const val SOURCE_INTENT = "source_intent"
        const val PORTFOLIO_ID = "portfolio_id"
        const val PORTFOLIO_NAME = "portfolio_name"
        const val USER_ID = "user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.srlPortfolio.setOnRefreshListener {
            refreshMyPortfolioData()
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            val sourceIntent = intent.getStringExtra(SOURCE_INTENT)
            if (sourceIntent == "profileFragment") {
                tvTitle.setText(R.string.my_portfolio)

                portfolioAdapter = PortfolioAdapter(
                    onItemClick = { portfolio ->
                        val intent = Intent(this@PortfolioActivity, PortfolioDetailActivity::class.java)
                        intent.putExtra(PortfolioDetailActivity.PORTFOLIO_ID, portfolio.portfolioId)
                        startActivity(intent)
                    },
                    onHoldClick = { portfolio ->
                        val intent = Intent(this@PortfolioActivity, AddPortfolioActivity::class.java)
                        intent.putExtra(AddPortfolioActivity.PORTFOLIO_ID, portfolio.portfolioId)
                        startActivity(intent)
                    }
                )
            } else if (sourceIntent == "createServiceActivity") {
                tvTitle.setText(R.string.add_portfolio)
                btnAddPortofolio.visibility = View.GONE

                portfolioAdapter = PortfolioAdapter(
                    onItemClick = { portfolio ->
                        setResult(RESULT_OK, Intent().apply {
                            putExtra(PORTFOLIO_NAME, portfolio.portfolioTitle)
                            putExtra(PORTFOLIO_ID, portfolio.portfolioId)
                        })
                        finish()
                    },
                    onHoldClick = { }
                )
            } else {
                tvTitle.setText(R.string.portfolio_user)
                btnAddPortofolio.visibility = View.GONE

                portfolioAdapter = PortfolioAdapter(
                    onItemClick = { portfolio ->
                        val intent = Intent(this@PortfolioActivity, PortfolioDetailActivity::class.java)
                        intent.putExtra(PortfolioDetailActivity.PORTFOLIO_ID, portfolio.portfolioId)
                        startActivity(intent)
                    },
                    onHoldClick = { }
                )
            }

            btnBack.setOnClickListener {
                finish()
            }

            btnAddPortofolio.setOnClickListener {
                val intent = Intent(this@PortfolioActivity, AddPortfolioActivity::class.java)
                startActivity(intent)
            }

            rvPortfolio.apply {
                layoutManager = GridLayoutManager(this@PortfolioActivity, 2)
                adapter = portfolioAdapter
            }

        }
    }

    private fun observeViewModel() {
        portfolioViewModel.apply {
            portfolios.observe(this@PortfolioActivity) { newPortfolioList ->
                Log.d(TAG, "Fetched Portfolios: $newPortfolioList")
                Log.d(TAG, "Portfolios to Adapter: ${newPortfolioList.size}")
                portfolioAdapter.submitList(newPortfolioList)
                portfolioAdapter.notifyDataSetChanged()

                if (newPortfolioList.isEmpty()) {
                    binding.llEmptyPortfolio.visibility = View.VISIBLE
                } else {
                    binding.llEmptyPortfolio.visibility = View.GONE
                }
            }

            operationResult.observe(this@PortfolioActivity) { (success, message) ->
                if (!success) {
                    showToast(message ?: "Terjadi kesalahan")
                } else {
                    refreshMyPortfolioData()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun refreshMyPortfolioData() {
        val userId = intent.getStringExtra(USER_ID)
        binding.srlPortfolio.isRefreshing = true

        if (userId != null) {
            portfolioViewModel.fetchUserPortfolios(userId)
        } else {
            finish()
        }

        lifecycleScope.launch {
            binding.srlPortfolio.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMyPortfolioData()
    }
}