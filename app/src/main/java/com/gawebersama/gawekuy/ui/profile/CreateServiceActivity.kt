package com.gawebersama.gawekuy.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.ServiceSelectionAdapter
import com.gawebersama.gawekuy.data.adapter.ServiceTagsAdapter
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.util.ServiceTypeDiffCallback
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.data.viewmodel.StorageViewModel
import com.gawebersama.gawekuy.databinding.ActivityCreateServiceBinding
import com.gawebersama.gawekuy.databinding.DialogDeleteServiceBinding
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class CreateServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateServiceBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private lateinit var serviceSelectionAdapter: ServiceSelectionAdapter
    private val serviceSelectionList = mutableListOf<ServiceSelectionModel>()

    private lateinit var serviceTagAdapter: ServiceTagsAdapter
    private val serviceTagList = mutableListOf<String>()

    private var imageUri: Uri? = null
    private var serviceId: String? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                binding.tietFileName.setText(uri.lastPathSegment)
            }
        }
    }

    val categoryList = listOf<String>("Penulisan & Akademik", "Desain & Multimedia", "Pengembangan Teknologi", "Pemasaran & Media Sosial", "Lainnya")

    companion object {
        const val SERVICE_ID = "SERVICE_ID"
        const val TAG = "CreateServiceActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateServiceBinding.inflate(layoutInflater)
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
        } else {
            Log.d(TAG, "Service ID is null")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryList)
        with(binding) {
            spinnerCategory.setAdapter(adapter)
            spinnerCategory.setOnItemClickListener { _, _, position, _ ->
                categoryList[position]
            }
            spinnerCategory.setDropDownBackgroundResource(R.drawable.dropdown_background)
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            val flexBoxLayoutManager = FlexboxLayoutManager(this@CreateServiceActivity).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }

            tvTitle.text = if (serviceId != null) "Edit Jasa" else "Buat Jasa"
            btnCreate.text = if (serviceId != null) "Simpan Perubahan" else "Buat Jasa"
            btnDelete.visibility = if (serviceId != null) View.VISIBLE else View.GONE

            if (serviceId == null) {
                serviceSelectionList.add(ServiceSelectionModel("", 0.0))
            }

            btnBack.setOnClickListener { finish() }
            llPortfolio.setOnClickListener {
                val intent = Intent(this@CreateServiceActivity, AddingPortfolioActivity::class.java)
                startActivity(intent)
            }

            serviceSelectionAdapter = ServiceSelectionAdapter(serviceSelectionList) { position ->
                if (position in serviceSelectionList.indices) {
                    serviceSelectionList.removeAt(position)
                    serviceSelectionAdapter.notifyItemRemoved(position)
                    serviceSelectionAdapter.notifyItemRangeChanged(position, serviceSelectionList.size)
                }
            }

            rvServiceSelection.apply {
                layoutManager = LinearLayoutManager(this@CreateServiceActivity)
                adapter = serviceSelectionAdapter
                setHasFixedSize(false)
            }

            tvAddService.setOnClickListener {
                val newService = ServiceSelectionModel("", 0.0)
                serviceSelectionList.add(newService)
                serviceSelectionAdapter.notifyItemInserted(serviceSelectionList.size - 1)
                Log.d(TAG, "Service List: $serviceSelectionList")
            }

            serviceTagAdapter = ServiceTagsAdapter(serviceTagList) { position ->
                if (position in serviceTagList.indices) {
                    serviceTagList.removeAt(position)
                    serviceTagAdapter.notifyItemRemoved(position)
                    serviceTagAdapter.notifyItemRangeChanged(position, serviceTagList.size)
                }
            }

            rvTags.apply {
                layoutManager = flexBoxLayoutManager
                adapter = serviceTagAdapter
                setHasFixedSize(false)
            }

            fabAddTags.setOnClickListener {
                val newTag = binding.tietServiceTags.text.toString().trim()

                if (newTag.isNotEmpty()) {
                    serviceTagList.add(newTag)
                    serviceTagAdapter.notifyItemInserted(serviceTagList.size - 1)
                    binding.tietServiceTags.text?.clear()
                }
            }

            btnSelectFile.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@CreateServiceActivity)
                        .crop()
                        .galleryOnly()
                        .createIntent()
                )
            }

            btnCreate.setOnClickListener {
                val serviceName = tietServiceName.text.toString().trim()
                val serviceDescription = tietDesc.text.toString().trim()
                val selectedCategory = spinnerCategory.text.toString().trim()
                val oldServiceImageUrl = serviceViewModel.imageBannerUrl.value ?: ""

                if (serviceName.isEmpty()) {
                    tietServiceName.error = "Nama tidak boleh kosong"
                    return@setOnClickListener
                }

                if (serviceSelectionList.isEmpty()) {
                    showToast("Harus ada minimal satu layanan")
                    return@setOnClickListener
                }

                if (serviceSelectionList.any { it.name.isEmpty() || it.price == 0.0 }) {
                    showToast("Nama dan harga layanan tidak boleh kosong")
                    return@setOnClickListener
                }

                if (selectedCategory.isEmpty()) {
                    showToast("Kategori tidak boleh kosong")
                    return@setOnClickListener
                }

                binding.progressBar.visibility = View.VISIBLE
                Log.d(TAG, "Service Selection List Before Saving: $serviceSelectionList")

                if (imageUri != null) {
                    uploadImageToSupabase(imageUri!!) { imageUrl ->
                        if (serviceId != null) {
                            serviceViewModel.updateService(
                                serviceId = serviceId!!,
                                serviceName = serviceName,
                                serviceDesc = serviceDescription,
                                imageBannerUrl = imageUrl,
                                serviceCategory = selectedCategory,
                                serviceTypes = serviceSelectionList,
                                serviceTags = serviceTagList
                            )
                            deleteOldServiceImage(oldServiceImageUrl)
                        } else {
                            serviceViewModel.createService(
                                serviceName = serviceName,
                                serviceDesc = serviceDescription,
                                imageBannerUrl = imageUrl,
                                serviceCategory = selectedCategory,
                                serviceTypes = serviceSelectionList,
                                serviceTags = serviceTagList
                            )
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    if (serviceId != null) {
                        serviceViewModel.updateService(
                            serviceId = serviceId!!,
                            serviceName = serviceName,
                            serviceDesc = serviceDescription,
                            imageBannerUrl = oldServiceImageUrl,
                            serviceCategory = selectedCategory,
                            serviceTypes = serviceSelectionList,
                            serviceTags = serviceTagList
                        )
                    } else {
                        serviceViewModel.createService(
                            serviceName = serviceName,
                            serviceDesc = serviceDescription,
                            imageBannerUrl = oldServiceImageUrl,
                            serviceCategory = selectedCategory,
                            serviceTypes = serviceSelectionList,
                            serviceTags = serviceTagList
                        )
                    }
                    setResult(RESULT_OK)
                    finish()
                }
            }

            btnDelete.setOnClickListener {
                showDeleteDialog()
            }
        }
    }

    private fun showDeleteDialog() {
        val dialogBinding = DialogDeleteServiceBinding.inflate(layoutInflater)
        val deleteDialog = AlertDialog.Builder(this@CreateServiceActivity).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener {
                deleteDialog.dismiss()
            }

            btnDelete.setOnClickListener {
                val oldServiceImageUrl = serviceViewModel.imageBannerUrl.value ?: ""

                if (serviceId != null) {
                    deleteOldServiceImage(oldServiceImageUrl)
                    serviceViewModel.deleteService(serviceId!!)
                    setResult(RESULT_OK)
                    finish()
                }

                deleteDialog.dismiss()
            }

            deleteDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            deleteDialog.show()
        }
    }

    private fun observeViewModels() {
        serviceViewModel.apply {
            operationResult.observe(this@CreateServiceActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showToast(message ?: "Terjadi kesalahan")
                }
            }

            serviceWithUser.observe(this@CreateServiceActivity) { services ->
                binding.tietFileName.setText(services?.firstOrNull()?.service?.imageBannerUrl ?: "")
                Log.d(TAG, "Fetched Services: $services")
            }

            serviceName.observe(this@CreateServiceActivity) { binding.tietServiceName.setText(it) }
            serviceDesc.observe(this@CreateServiceActivity) { binding.tietDesc.setText(it) }
            serviceCategory.observe(this@CreateServiceActivity) { binding.spinnerCategory.setText(it, false) }

            services.observe(this@CreateServiceActivity) { services ->
                Log.d(TAG, "Fetched Services X: $services")
            }

            imageBannerUrl.observe(this@CreateServiceActivity) { imageUrl ->
                if (imageUrl != null) {
                    binding.tietFileName.setText(imageUrl.substringAfterLast("/"))
                }
            }

            serviceTypes.observe(this@CreateServiceActivity) { newServiceTypes ->
                newServiceTypes?.let {
                    val diffCallback = ServiceTypeDiffCallback(serviceSelectionList, newServiceTypes)
                    val diffResult = DiffUtil.calculateDiff(diffCallback)

                    serviceSelectionList.clear()
                    serviceSelectionList.addAll(newServiceTypes)
                    diffResult.dispatchUpdatesTo(serviceSelectionAdapter)
                    Log.d(TAG, "Service Types Updated: $newServiceTypes")
                }
            }

            serviceTags.observe(this@CreateServiceActivity) { newServiceTags ->
                newServiceTags?.let {
                    serviceTagList.clear()
                    serviceTagList.addAll(newServiceTags)
                    serviceTagAdapter.notifyItemRangeChanged(0, serviceTagList.size)
                    Log.d(TAG, "Service Tags Updated: $newServiceTags")
                }
            }
        }

        storageViewModel.uploadStatus.observe(this@CreateServiceActivity) { success ->
            if (success) showToast("Foto berhasil diunggah") else showToast("Gagal mengunggah foto")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri) ?: return showToast("Gagal membaca file")
        val fileName = "service_${System.currentTimeMillis()}.jpg"
        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "service_banner", fileName)

        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            binding.progressBar.visibility = View.GONE
            if (!imageUrl.isNullOrEmpty()) {
                onSuccess(imageUrl)
            } else {
                showToast("Gagal mengunggah gambar")
            }
        }
    }

    private fun deleteOldServiceImage(oldImageUrl: String) {
        if (oldImageUrl.isNotEmpty()) {
            val fileName = oldImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "service_banner")
        }
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }
}
