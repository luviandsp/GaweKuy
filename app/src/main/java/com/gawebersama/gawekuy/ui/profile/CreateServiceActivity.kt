package com.gawebersama.gawekuy.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.data.adapter.ServiceSelectionAdapter
import com.gawebersama.gawekuy.data.dataclass.ServiceSelectionModel
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.data.viewmodel.StorageViewModel
import com.gawebersama.gawekuy.databinding.ActivityCreateServiceBinding
import com.github.drjacky.imagepicker.ImagePicker

class CreateServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateServiceBinding
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private lateinit var serviceSelectionAdapter: ServiceSelectionAdapter
    private val serviceSelectionList = mutableListOf<ServiceSelectionModel>()

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

        serviceId = intent.getStringExtra(SERVICE_ID) // Ambil serviceId dari intent jika ada

        Log.d("CreateServiceActivity", "Service ID received: $serviceId") // Debugging log
        if (serviceId != null) {
            serviceViewModel.fetchServiceById(serviceId!!)
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            tvTitle.text = if (serviceId != null) "Edit Jasa" else "Buat Jasa"
            btnCreate.text = if (serviceId != null) "Simpan Perubahan" else "Buat Jasa"
            btnDelete.visibility = if (serviceId != null) View.VISIBLE else View.GONE

            if (serviceId == null) {
                serviceSelectionList.add(ServiceSelectionModel("", 0.0))
            }

            btnBack.setOnClickListener { finish() }

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
                val oldServiceImageUrl = serviceViewModel.imageBannerUrl.value ?: ""

                if (serviceName.isEmpty()) {
                    tietServiceName.error = "Nama tidak boleh kosong"
                    return@setOnClickListener
                }

                if (serviceSelectionList.isEmpty()) {
                    showToast("Harus ada minimal satu layanan")
                    return@setOnClickListener
                }

                binding.progressBar.visibility = View.VISIBLE
                Log.d(TAG, "Service Selection List Before Saving: $serviceSelectionList")

                if (imageUri != null) {
                    uploadImageToSupabase(imageUri!!) { imageUrl ->
                        if (serviceId != null) {
                            serviceViewModel.updateService(serviceId!!, serviceName, serviceDescription, imageUrl, serviceSelectionList)
                            deleteOldServiceImage(oldServiceImageUrl)
                        } else {
                            serviceViewModel.createService(serviceName, serviceDescription, imageUrl, serviceSelectionList)
                        }
                        finish()
                    }
                } else {
                    if (serviceId != null) {
                        serviceViewModel.updateService(serviceId!!, serviceName, serviceDescription, oldServiceImageUrl, serviceSelectionList)
                    } else {
                        serviceViewModel.createService(serviceName, serviceDescription, oldServiceImageUrl, serviceSelectionList)
                    }
                    finish()
                }
            }

            btnDelete.setOnClickListener {

                val oldServiceImageUrl = serviceViewModel.imageBannerUrl.value ?: ""

                if (serviceId != null) {
                    deleteOldServiceImage(oldServiceImageUrl)
                    serviceViewModel.deleteService(serviceId!!)
                    finish()
                }
            }
        }
    }

    private fun observeViewModels() {
        serviceViewModel.apply {
            operationResult.observe(this@CreateServiceActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    finish()
                } else {
                    showToast(message ?: "Terjadi kesalahan")
                }
            }

            serviceName.observe(this@CreateServiceActivity) { binding.tietServiceName.setText(it) }
            serviceDesc.observe(this@CreateServiceActivity) { binding.tietDesc.setText(it) }

            imageBannerUrl.observe(this@CreateServiceActivity) { imageUrl ->
                if (imageUrl != null) {
                    binding.tietFileName.setText(imageUrl.substringAfterLast("/"))
                }
            }

            serviceTypes.observe(this@CreateServiceActivity) { serviceTypes ->
                if (serviceTypes != null) {
                    serviceSelectionList.clear()
                    serviceSelectionList.addAll(serviceTypes)
                    serviceSelectionAdapter.notifyDataSetChanged()
                    Log.d(TAG, "Service Types: $serviceTypes")
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
