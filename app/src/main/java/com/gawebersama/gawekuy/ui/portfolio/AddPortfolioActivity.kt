package com.gawebersama.gawekuy.ui.portfolio

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.gawebersama.gawekuy.data.viewmodel.PortfolioViewModel
import com.gawebersama.gawekuy.data.viewmodel.StorageViewModel
import com.gawebersama.gawekuy.databinding.ActivityAddPortfolioBinding
import com.gawebersama.gawekuy.databinding.DialogDeletePortfolioBinding
import com.github.drjacky.imagepicker.ImagePicker

class AddPortfolioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPortfolioBinding

    private val portfolioViewModel by viewModels<PortfolioViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

//    private lateinit var imagesAdapter: PortfolioDocumentImagesAdapter
//    private val imagesUriList = mutableListOf<Uri>()

    private var imageBannerUri: Uri? = null
    private var portfolioId: String? = null

//    private var position: Int = 0
//    private var addNewOrNo : Boolean = false

    private val launcherBanner = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                imageBannerUri = uri
                binding.tietFileBanner.setText(uri.lastPathSegment)
            }
        }
    }

//    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            result.data?.data?.let { uri ->
//                if (addNewOrNo == false) {
//                    imagesUriList[position] = uri
//                } else {
//                    imagesUriList.add(uri)
//                }
//                imagesAdapter.notifyDataSetChanged()
//                Log.d(TAG, "imagesUriList: $imagesUriList")
//            }
//        }
//    }

    companion object {
        const val TAG = "AddPortfolioActivity"
        const val PORTFOLIO_ID = "portfolio_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPortfolioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        portfolioId = intent.getStringExtra(PORTFOLIO_ID)

        if (portfolioId != null) {
            portfolioViewModel.fetchPortfolioById(portfolioId!!)
        } else {
            Log.d(TAG, "Portfolio ID is null")
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

            tvTitle.text = if (portfolioId != null) "Edit Portfolio" else "Buat Portfolio"
            btnCreate.text = if (portfolioId != null) "Simpan Perubahan" else "Buat Portfolio"
            btnDelete.visibility = if (portfolioId != null) View.VISIBLE else View.GONE

            btnSelectBanner.setOnClickListener {
                launcherBanner.launch(
                    ImagePicker.with(this@AddPortfolioActivity)
                        .crop()
                        .galleryOnly()
                        .createIntent()
                )
            }

            btnCreate.setOnClickListener { savePortfolio() }
            btnDelete.setOnClickListener { showDeleteDialog() }

//            imagesAdapter = PortfolioDocumentImagesAdapter(
//                images = imagesUriList,
//                onClickListener = { position ->
//                    addNewOrNo = false
//                    this@AddPortfolioActivity.position = position
//                    imagePickerLauncher.launch(
//                        ImagePicker.with(this@AddPortfolioActivity)
//                            .crop()
//                            .galleryOnly()
//                            .createIntent()
//                    )
//                },
//                onDeleteClick = { position ->
//                    if (position < imagesUriList.size) {
//                        imagesUriList.removeAt(position)
//                        imagesAdapter.notifyDataSetChanged()
//                    }
//                }
//            )

//            rvPortfolioImages.apply {
//                adapter = imagesAdapter
//                layoutManager = LinearLayoutManager(this@AddPortfolioActivity)
//                setHasFixedSize(false)
//            }

//            tvAddPortfolioImages.setOnClickListener {
//                addNewOrNo = true
//                imagePickerLauncher.launch(
//                    ImagePicker.with(this@AddPortfolioActivity)
//                        .crop()
//                        .galleryOnly()
//                        .createIntent()
//                )
//            }
        }
    }

    private fun observeViewModel() {
        portfolioViewModel.apply {
            operationResult.observe(this@AddPortfolioActivity) { (success, message) ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showToast(message ?: "Terjadi kesalahan")
                }
            }

            portfolioName.observe(this@AddPortfolioActivity) { binding.tietPortfolioName.setText(it) }
            portfolioDesc.observe(this@AddPortfolioActivity) { binding.tietDesc.setText(it) }

            imageBannerUrl.observe(this@AddPortfolioActivity) { imageUrl ->
                if (imageUrl != null) {
                    binding.tietFileBanner.setText(imageUrl.substringAfterLast("/"))
                }
            }

//            portfolioImageUrl.observe(this@AddPortfolioActivity) { imageUrls ->
//                imagesUriList.clear()
//                imageUrls?.forEach { imageUrl ->
//                    imagesUriList.add(imageUrl.toUri())
//                    imagesAdapter.notifyDataSetChanged()
//                }
//            }
        }
    }

    private fun savePortfolio() {
        val portfolioName = binding.tietPortfolioName.text.toString().trim()
        val portfolioDesc = binding.tietDesc.text.toString().trim()
        val oldPortfolioImageBanner = portfolioViewModel.imageBannerUrl.value ?: ""
//        val oldImagesUrl = portfolioViewModel.portfolioImageUrl.value ?: emptyList()

        if (portfolioName.isEmpty()) {
            binding.tietPortfolioName.error = "Nama tidak boleh kosong"
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        if (portfolioId != null) {
            updatePortfolio(
                portfolioId = portfolioId!!,
                portfolioName = portfolioName,
                portfolioDesc = portfolioDesc,
                oldPortfolioBannerImage = oldPortfolioImageBanner
            )
        } else {
            createPortfolio(
                portfolioName = portfolioName,
                portfolioDesc = portfolioDesc
            )
        }
    }

    private fun createPortfolio(portfolioName : String, portfolioDesc : String) {
        if(imageBannerUri != null) {
            uploadImageToSupabase(imageBannerUri) { imageUrl ->
                portfolioViewModel.createPortfolio(
                    portfolioName = portfolioName,
                    portfolioDesc = portfolioDesc,
                    portfolioBannerImage = imageUrl
                )
                setResult(RESULT_OK)
                finish()
            }
        } else {
            portfolioViewModel.createPortfolio(
                portfolioName = portfolioName,
                portfolioDesc = portfolioDesc,
                portfolioBannerImage = ""
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun updatePortfolio(portfolioId : String, portfolioName : String, portfolioDesc : String, oldPortfolioBannerImage : String) {
        if(imageBannerUri != null) {
            uploadImageToSupabase(imageBannerUri) { imageUrl ->
                portfolioViewModel.updatePortfolio(
                    portfolioId = portfolioId,
                    portfolioName = portfolioName,
                    portfolioDesc = portfolioDesc,
                    portfolioBannerImage = imageUrl
                )
                deleteOldPortfolioImages(oldPortfolioBannerImage)
                setResult(RESULT_OK)
                finish()
            }
        } else {
            portfolioViewModel.updatePortfolio(
                portfolioId = portfolioId,
                portfolioName = portfolioName,
                portfolioDesc = portfolioDesc,
                portfolioBannerImage = oldPortfolioBannerImage
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showDeleteDialog() {
        val dialogBinding = DialogDeletePortfolioBinding.inflate(layoutInflater)
        val deleteDialog = AlertDialog.Builder(this@AddPortfolioActivity).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener { deleteDialog.dismiss() }

            val oldBannerUrl = portfolioViewModel.imageBannerUrl.value ?: ""

            btnDelete.setOnClickListener {
                portfolioId?.let {
                    deleteOldPortfolioImages(oldBannerUrl)
                    portfolioViewModel.deletePortfolio(it)
                    setResult(RESULT_OK)
                    finish()
                }
                deleteDialog.dismiss()
            }

            deleteDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            deleteDialog.show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToSupabase(uri: Uri?, onSuccess: (String) -> Unit) {
        if (uri == null) {
            onSuccess("")
            return
        }

        val inputStream = contentResolver.openInputStream(uri) ?: return showToast("Gagal membaca file")
        val fileName = "portfolio_${System.currentTimeMillis()}.jpg"
        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "portfolio_images", fileName)

        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            binding.progressBar.visibility = View.GONE
            if (!imageUrl.isNullOrEmpty()) {
                onSuccess(imageUrl)
            } else {
                showToast("Gagal mengunggah gambar")
            }
        }
    }

    private fun deleteOldPortfolioImages(oldBannerUrl: String) {
        if (oldBannerUrl.isNotEmpty()) {
            val fileName = oldBannerUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "portfolio_images")
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
