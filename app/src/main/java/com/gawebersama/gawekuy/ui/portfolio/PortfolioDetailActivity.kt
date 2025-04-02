package com.gawebersama.gawekuy.ui.portfolio

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.viewmodel.PortfolioViewModel
import com.gawebersama.gawekuy.databinding.ActivityPortfolioDetailBinding

class PortfolioDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortfolioDetailBinding
    private val portfolioViewModel by viewModels<PortfolioViewModel>()

//    private lateinit var portfolioImagesAdapter: PortfolioImagesAdapter
//    private var portfolioImagesList= mutableListOf<String>()

    companion object {
        const val TAG = "PortfolioDetailActivity"
        const val PORTFOLIO_ID = "portfolio_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val portfolioId = intent.getStringExtra(PORTFOLIO_ID)
        if (portfolioId != null) {
            portfolioViewModel.fetchPortfolioById(portfolioId)
        }

        Log.d(TAG, "Portfolio ID received: $portfolioId")

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

//            portfolioImagesAdapter = PortfolioImagesAdapter(portfolioImagesList)
//            rvPortfolioImage.apply {
//                layoutManager = LinearLayoutManager(this@PortfolioDetailActivity)
//                adapter = portfolioImagesAdapter
//                setHasFixedSize(false)
//            }
        }
    }

    private fun observeViewModel() {
        portfolioViewModel.apply {
//            portfolioImageUrl.observe(this@PortfolioDetailActivity) { newPortfolioImages ->
//                newPortfolioImages?.let {
//                    portfolioImagesList.clear()
//                    portfolioImagesList.addAll(newPortfolioImages)
//                    Log.d(TAG, "Portfolio Images Updated: $newPortfolioImages")
//                    portfolioImagesAdapter.notifyDataSetChanged()
//                }
//            }

            imageBannerUrl.observe(this@PortfolioDetailActivity) { newImageBannerUrl ->
                newImageBannerUrl?.let {
                    Glide.with(this@PortfolioDetailActivity)
                        .load(newImageBannerUrl)
                        .into(binding.ivPortofolioImages)
                    Log.d(TAG, "Image Banner URL Updated: $newImageBannerUrl")
                }
            }

            portfolioName.observe(this@PortfolioDetailActivity) { newPortfolioName ->
                newPortfolioName?.let {
                    binding.tvTitlePortfolio.text = newPortfolioName
                    Log.d(TAG, "Portfolio Name Updated: $newPortfolioName")
                }
            }

            portfolioDesc.observe(this@PortfolioDetailActivity) { newPortfolioDescription ->
                newPortfolioDescription?.let {
                    binding.tvPortfolioDesc.text = newPortfolioDescription
                    Log.d(TAG, "Portfolio Description Updated: $newPortfolioDescription")
                }
            }
        }
    }
}