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
import com.gawebersama.gawekuy.BuildConfig
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.ServiceSelectedAdapter
import com.gawebersama.gawekuy.data.adapter.ServiceShowTagsAdapter
import com.gawebersama.gawekuy.data.api.RetrofitClient
import com.gawebersama.gawekuy.data.datamodel.CustomerDetails
import com.gawebersama.gawekuy.data.datamodel.ItemDetails
import com.gawebersama.gawekuy.data.datamodel.MidtransRequest
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivityServiceDetailBinding
import com.gawebersama.gawekuy.databinding.BottomSheetDialogOrderDetailBinding
import com.gawebersama.gawekuy.ui.portfolio.FreelancerPortfolioActivity
import com.gawebersama.gawekuy.ui.profile.FreelancerProfileActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ServiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceDetailBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private val userViewModel by viewModels<UserViewModel>()
    private val transactionViewModel by viewModels<TransactionViewModel>()

    private lateinit var serviceTagAdapter: ServiceShowTagsAdapter
    private val serviceTagList = mutableListOf<String>()

    private lateinit var serviceSelectedAdapter: ServiceSelectedAdapter
    private val serviceSelectedList = mutableListOf<ServiceSelectionModel>()

    private var serviceId: String? = null

    private var orderId : String = ""
    private var grossAmount : Double = 0.0
    private var customerName : String = ""
    private var customerEmail : String = ""
    private var customerPhone : String = ""
    private var thisServiceCategory : String = ""

    companion object {
        const val SERVICE_ID = "service_id"
        const val TAG = "ServiceDetailActivity"
        private const val SERVICE_FEE_MULTIPLIER = 0.1
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

                showBottomSheetDialog(selectedService)
            }
        }
    }

    private fun showBottomSheetDialog(selectedService: ServiceSelectionModel) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetDialogOrderDetailBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(sheetBinding.root)
        bottomSheetDialog.show()

        with(sheetBinding) {
            val formatter = DecimalFormat("#,###")

            tvServiceName.text = serviceViewModel.serviceName.value
            tvSelectedServiceName.text = selectedService.name

            tvServicePrice.text = getString(R.string.price_format, formatter.format(selectedService.price))
            tvAppService.text = getString(R.string.price_format, formatter.format(selectedService.price * 0.1))
            tvTotalPayment.text = getString(R.string.price_format, formatter.format(selectedService.price + (selectedService.price * 0.1)))

            btnPay.setOnClickListener {
                orderId = "ORDER-${System.currentTimeMillis()}"
                grossAmount = selectedService.price + (selectedService.price * SERVICE_FEE_MULTIPLIER)

                val request = MidtransRequest(
                    orderId = orderId,
                    grossAmount = grossAmount.roundToInt().toInt(),
                    customerDetails = CustomerDetails(
                        firstName = customerName,
                        lastName = "",
                        email = customerEmail,
                        phone = customerPhone
                    ),
                    itemDetails = listOf(
                        ItemDetails(
                            id = serviceId ?: "",
                            price = selectedService.price.toInt(),
                            quantity = 1,
                            name = selectedService.name,
                            category = thisServiceCategory
                        )
                    )
                )

                Log.d(TAG, "Request: $request")
                callMidtrans()

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.createTransaction(request)
                        Log.d(TAG, "Response: $response")
                        Log.d(TAG, "Response Body: ${response.body()}")
                        if (response.isSuccessful && response.body() != null) {
                            val snapToken = response.body()!!.token
                            val sdk = MidtransSDK.getInstance()
                            if (sdk != null) {
                                sdk.startPaymentUiFlow(this@ServiceDetailActivity, snapToken)
                                saveTransactionToFirebase()
                                bottomSheetDialog.dismiss()
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

    private fun saveTransactionToFirebase() {
        val buyerId = userViewModel.userId.value
        val buyerName = userViewModel.userName.value
        val buyerEmail = userViewModel.userEmail.value
        val sellerId = serviceViewModel.ownerServiceId.value
        val sellerName = serviceViewModel.ownerServiceName.value
        val sellerEmail = serviceViewModel.ownerServiceEmail.value

        if (buyerId == null || buyerName == null || buyerEmail == null ||
            sellerId == null || sellerName == null || sellerEmail == null) {
            Toast.makeText(this, "Data transaksi belum lengkap", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Missing user/service data for transaction")
        } else {
            transactionViewModel.saveTransaction(
                orderId = orderId,
                serviceId = serviceId,
                grossAmount = grossAmount,
                status = "pending",
                buyerId = buyerId,
                buyerName = buyerName,
                buyerEmail = buyerEmail,
                sellerId = sellerId,
                sellerName = sellerName,
                sellerEmail = sellerEmail,
                transactionTime = Timestamp.now()
            )
        }
    }

    private fun callMidtrans() {
        SdkUIFlowBuilder.init()
            .setContext(this@ServiceDetailActivity)
            .setClientKey(BuildConfig.MIDTRANS_CLIENT_KEY_SANDBOX)
            .setMerchantBaseUrl(BuildConfig.BACKEND_BASE_URL)
            .enableLog(true)
            .setColorTheme(CustomColorTheme("#0084FF", "#004c94", "#0084FF"))
            .setLanguage("id")
            .setTransactionFinishedCallback { result ->
                when {
                    result.status != null -> {
                        if (result.status == TransactionResult.STATUS_SUCCESS) {
                            Toast.makeText(this, "Transaksi berhasil!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Status: ${result.status}", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Status transaksi bukan success")
                        }
                    }
                    result.isTransactionCanceled -> {
                        Toast.makeText(this, "Transaksi dibatalkan", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Transaksi gagal atau belum selesai", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .buildSDK()
    }


    private fun observeViewModels() {
        serviceViewModel.apply {
            serviceName.observe(this@ServiceDetailActivity) { binding.tvServiceName.text = it }
            serviceDesc.observe(this@ServiceDetailActivity) { binding.tvServiceDesc.text = it }

            serviceCategory.observe(this@ServiceDetailActivity) {
                thisServiceCategory = it
            }

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

            userPhone.observe(this@ServiceDetailActivity) { phone ->
                customerPhone = phone ?: ""
            }
        }
    }
}
