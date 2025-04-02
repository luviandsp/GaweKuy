package com.gawebersama.gawekuy.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityFreelancerProfileBinding
import com.gawebersama.gawekuy.ui.portfolio.PortfolioActivity

class FreelancerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFreelancerProfileBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()

    companion object {
        const val TAG = "ProfileUserActivity"
        const val SERVICE_ID = "serviceId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreelancerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serviceId = intent.getStringExtra(SERVICE_ID)
        if (serviceId != null) {
            serviceViewModel.fetchServiceById(serviceId)
        }


        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViewModels() {
        serviceViewModel.apply {
            ownerServiceName.observe(this@FreelancerProfileActivity) { name ->
                binding.tvNickname.text = name ?: ""
            }

            ownerServicePhone.observe(this@FreelancerProfileActivity) { phone ->
                binding.btnChat.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri()))
                }
            }

            ownerServiceImage.observe(this@FreelancerProfileActivity) { imageUrl ->
                Glide.with(this@FreelancerProfileActivity)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            ownerServiceBio.observe(this@FreelancerProfileActivity) { bio ->
                binding.tvUserBio.text = bio ?: ""
            }

            ownerStatus.observe(this@FreelancerProfileActivity) { status ->
                binding.tvStatus.text = status
            }

            ownerServiceId.observe(this@FreelancerProfileActivity) { userId ->
                binding.llPortfolio.setOnClickListener {
                    val intent = Intent(this@FreelancerProfileActivity, PortfolioActivity::class.java)
                    intent.putExtra(PortfolioActivity.USER_ID, userId)
                    startActivity(intent)
                }
            }
        }
    }
}