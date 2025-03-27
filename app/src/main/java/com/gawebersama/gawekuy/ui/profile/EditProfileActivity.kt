package com.gawebersama.gawekuy.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.viewmodel.AuthViewModel
import com.gawebersama.gawekuy.data.viewmodel.StorageViewModel
import com.gawebersama.gawekuy.databinding.ActivityEditProfileBinding
import com.github.drjacky.imagepicker.ImagePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private var imageUri: Uri? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // Tampilkan gambar sementara sebelum upload
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfilePicture)

                imageUri = uri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            ivProfilePicture.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@EditProfileActivity)
                        .crop()
                        .cropOval()
                        .galleryOnly()
                        .createIntent()
                )
            }

            btnSave.setOnClickListener {
                val name = tietFullName.text.toString().trim()

                if (name.isEmpty()) {
                    tietFullName.error = "Nama tidak boleh kosong"
                    return@setOnClickListener
                }

                if (imageUri != null) {
                    uploadImageToSupabase(imageUri!!) { imageUrl ->
                        authViewModel.updateProfile(name, imageUrl)
                        finish()
                    }
                } else {
                    val oldProfileImageUrl = authViewModel.userImageUrl.value ?: ""
                    authViewModel.updateProfile(name, oldProfileImageUrl)
                    finish()
                }
            }
        }
    }

    private fun observeViewModels() {
        authViewModel.userImageUrl.observe(this) { imageUrl ->
            if (imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            } else {
                Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }
        }

        storageViewModel.imageUrl.observe(this) { newImageUrl ->
            if (!newImageUrl.isNullOrEmpty()) {
                // Simpan URL ke Firestore
                CoroutineScope(Dispatchers.IO).launch {
                    storageViewModel.saveImageUrlToFirestore(newImageUrl)
                }
            }
        }

        authViewModel.userName.observe(this@EditProfileActivity) { name ->
            name?.let {
                binding.tietFullName.setText(it)
            }
        }
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val file = File(uri.path ?: return)
        val fileName = "profile_${System.currentTimeMillis()}.jpg"

        binding.progressBar.visibility = View.VISIBLE

        storageViewModel.uploadImage(file, "profile_pictures", fileName)

        storageViewModel.imageUrl.observe(this) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                storageViewModel.saveImageUrlToFirestore(imageUrl)
                binding.progressBar.visibility = View.GONE
                onSuccess(imageUrl) // Panggil callback setelah URL diperoleh
            }
        }
    }

}