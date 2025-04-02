package com.gawebersama.gawekuy.ui.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.ServiceSelectedAdapter
import com.gawebersama.gawekuy.data.adapter.ServiceShowTagsAdapter
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.databinding.ActivityServiceDetailBinding
import com.gawebersama.gawekuy.ui.portfolio.FreelancerPortfolioActivity
import com.gawebersama.gawekuy.ui.profile.FreelancerProfileActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ServiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceDetailBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()

    private lateinit var serviceTagAdapter: ServiceShowTagsAdapter
    private val serviceTagList = mutableListOf<String>()

    private lateinit var serviceSelectedAdapter: ServiceSelectedAdapter
    private val serviceSelectedList = mutableListOf<ServiceSelectionModel>()

    private var serviceId: String? = null

    companion object {
        const val SERVICE_ID = "service_id"
        const val TAG = "ServiceDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        serviceId = intent.getStringExtra(SERVICE_ID)
        Log.d(TAG, "Service ID received: $serviceId")
        if (serviceId != null) {
            serviceViewModel.fetchServiceById(serviceId!!)
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            val flexBoxLayoutManager = FlexboxLayoutManager(this@ServiceDetailActivity).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }

            serviceTagAdapter = ServiceShowTagsAdapter(serviceTagList)
            rvTags.apply {
                layoutManager = flexBoxLayoutManager
                adapter = serviceTagAdapter
                setHasFixedSize(true)
            }

            serviceSelectedAdapter = ServiceSelectedAdapter(serviceSelectedList) { position, isChecked ->
                serviceSelectedList.forEachIndexed { index, service ->
                    service.isSelected = (index == position && isChecked)
                }
                serviceSelectedAdapter.notifyItemRangeChanged(0, serviceSelectedList.size)
            }

            rvServiceSelection.apply {
                layoutManager = LinearLayoutManager(this@ServiceDetailActivity)
                adapter = serviceSelectedAdapter
                setHasFixedSize(true)
            }

            llPortfolio.setOnClickListener {
                val intent = Intent(this@ServiceDetailActivity, FreelancerPortfolioActivity::class.java)
                intent.putExtra(FreelancerPortfolioActivity.SERVICE_ID, serviceId)
                startActivity(intent)
            }

            llOwnerProfile.setOnClickListener {
                val intent = Intent(this@ServiceDetailActivity, FreelancerProfileActivity::class.java)
                intent.putExtra(FreelancerProfileActivity.SERVICE_ID, serviceId)
                startActivity(intent)
            }

            btnBack.setOnClickListener { finish() }
        }
    }

    private fun observeViewModels() {
        serviceViewModel.apply {
            serviceName.observe(this@ServiceDetailActivity) { binding.tvServiceName.text = it }
            serviceDesc.observe(this@ServiceDetailActivity) { binding.tvServiceDesc.text = it }

            imageBannerUrl.observe(this@ServiceDetailActivity) { imageUrl ->
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    Glide.with(this@ServiceDetailActivity).load(imageUrl).into(binding.ivServiceBanner)
                } else {
                    Glide.with(this@ServiceDetailActivity).load(R.drawable.logo_background).into(binding.ivServiceBanner)
                }
            }

            serviceTags.observe(this@ServiceDetailActivity) { newServiceTags ->
                newServiceTags?.let {
                    serviceTagList.clear()
                    serviceTagList.addAll(newServiceTags)
                    serviceTagAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Service Tags Updated: $newServiceTags")
                }
            }

            ownerServiceImage.observe(this@ServiceDetailActivity) { imageUrl ->
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    Glide.with(this@ServiceDetailActivity).load(imageUrl).circleCrop()
                        .into(binding.ivProfilePicture)
                }
            }

            ownerServiceName.observe(this@ServiceDetailActivity) { name ->
                binding.tvFullName.text = name
            }

            ownerServiceAccountStatus.observe(this@ServiceDetailActivity) { status ->
                with(binding) {
                    if(status == true) {
                        tvStatusAccount.setText(R.string.active)
                        tvStatusAccount.setTextColor(ContextCompat.getColor(this@ServiceDetailActivity, R.color.active_color_text))
                        cvStatusAccount.setCardBackgroundColor(ContextCompat.getColor(this@ServiceDetailActivity, R.color.active_color))
                    } else {
                        tvStatusAccount.setText(R.string.inactive)
                        tvStatusAccount.setTextColor(ContextCompat.getColor(this@ServiceDetailActivity, R.color.inactive_color_text))
                        cvStatusAccount.setCardBackgroundColor(ContextCompat.getColor(this@ServiceDetailActivity, R.color.inactive_color))
                    }
                }
            }

            serviceTypes.observe(this@ServiceDetailActivity) { newServiceTypes ->
                newServiceTypes?.let {
                    serviceSelectedList.clear()
                    serviceSelectedList.addAll(newServiceTypes)
                    serviceSelectedAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Service Types Updated: $newServiceTypes")
                }
            }

        }
    }
}
