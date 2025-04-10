package com.gawebersama.gawekuy.ui.service

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.ServiceSelectedAdapter
import com.gawebersama.gawekuy.data.adapter.ServiceShowTagsAdapter
import com.gawebersama.gawekuy.data.api.RetrofitClient
import com.gawebersama.gawekuy.data.datamodel.CustomerDetails
import com.gawebersama.gawekuy.data.datamodel.MidtransRequest
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivityServiceDetailBinding
import com.gawebersama.gawekuy.ui.portfolio.FreelancerPortfolioActivity
import com.gawebersama.gawekuy.ui.profile.FreelancerProfileActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.midtrans.sdk.corekit.core.MidtransSDK
import kotlinx.coroutines.launch

class ServiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceDetailBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var serviceTagAdapter: ServiceShowTagsAdapter
    private val serviceTagList = mutableListOf<String>()

    private lateinit var serviceSelectedAdapter: ServiceSelectedAdapter
    private val serviceSelectedList = mutableListOf<ServiceSelectionModel>()

    private var serviceId: String? = null

    private var orderId : String = ""
    private var grossAmount : Double = 0.0
    private var customerName : String = ""
    private var customerEmail : String = ""

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

            btnOrderService.setOnClickListener {
                val selectedService = serviceSelectedList.find { it.isSelected }

                if (selectedService == null) {
                    Toast.makeText(this@ServiceDetailActivity, "Pilih salah satu jenis layanan terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                orderId = "ORDER-${System.currentTimeMillis()}"
                grossAmount = selectedService.price

                val request = MidtransRequest(
                    orderId = orderId,
                    grossAmount = grossAmount,
                    customerDetails = CustomerDetails(
                        name = customerName,
                        email = customerEmail
                    )
                )

                Log.d(TAG, "Request: $request")

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.createTransaction(request)
                        Log.d(TAG, "Response: $response")
                        Log.d(TAG, "Response Body: ${response.body()}")
                        if (response.isSuccessful && response.body() != null) {
                            val snapToken = response.body()!!.token
                            val sdk = MidtransSDK.getInstance()
                            if (sdk != null) {
                                sdk.startPaymentUiFlow(this@ServiceDetailActivity, snapToken)
                            } else {
                                Toast.makeText(this@ServiceDetailActivity, "Midtrans belum siap. Coba buka ulang aplikasi.", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "MidtransSDK is null, belum diinisialisasi.")
                            }
                        } else {
                            Toast.makeText(this@ServiceDetailActivity, "Gagal mendapatkan token pembayaran", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ServiceDetailActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error creating transaction", e)
                    }
                }
            }
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

            ownerServicePhone.observe(this@ServiceDetailActivity) { phone ->
                binding.btnChatFreelancer.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri()))
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

        userViewModel.apply {
            userName.observe(this@ServiceDetailActivity) { name ->
                customerName = name ?: ""
            }

            userEmail.observe(this@ServiceDetailActivity) { email ->
                customerEmail = email ?: ""
            }
        }
    }
}
