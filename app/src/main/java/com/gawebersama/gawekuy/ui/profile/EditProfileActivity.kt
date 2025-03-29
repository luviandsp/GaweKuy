package com.gawebersama.gawekuy.ui.profile

import android.net.Uri
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.enum.UserStatus
import com.gawebersama.gawekuy.data.viewmodel.StorageViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivityEditProfileBinding
import com.github.drjacky.imagepicker.ImagePicker

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val userViewModel by viewModels<UserViewModel>()
    private val storageViewModel by viewModels<StorageViewModel>()

    private var imageUri: Uri? = null
    private var isPhotoDeleted: Boolean = false

    val statusList = UserStatus.entries.map { it.name.lowercase().replace("_", " ").capitalizeWords() }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfilePicture)

                imageUri = uri
                isPhotoDeleted = false
            }
        }
    }

    companion object {
        const val RESULT_CODE = 110
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        binding.spinnerStatus.setAdapter(adapter)
        binding.spinnerStatus.setOnItemClickListener { _, _, position, _ ->
            statusList[position]
        }
        binding.spinnerStatus.setDropDownBackgroundResource(R.drawable.dropdown_background)

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            btnChangePhoto.setOnClickListener {
                launcher.launch(
                    ImagePicker.with(this@EditProfileActivity)
                        .crop()
                        .cropOval()
                        .galleryOnly()
                        .createIntent()
                )
            }

            btnDeletePhoto.setOnClickListener {
                isPhotoDeleted = true
                imageUri = null

                Glide.with(this@EditProfileActivity)
                    .load(R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            ccp.registerCarrierNumberEditText(tietPhoneNumber)

            btnSave.setOnClickListener {
                val name = tietFullName.text.toString().trim()
                val phone = ccp.fullNumber.trim()
                val biography = tietBiography.text.toString().trim()
                val selectedStatus = spinnerStatus.text.toString().trim()
                val oldProfileImageUrl = userViewModel.userImageUrl.value ?: ""

                if (name.isEmpty()) {
                    tietFullName.error = "Nama tidak boleh kosong"
                    return@setOnClickListener
                }

                if (phone.isEmpty()) {
                    tietPhoneNumber.error = "Nomor telepon tidak boleh kosong"
                    return@setOnClickListener
                }

                if (isPhotoDeleted) {
                    deleteProfilePhoto()
                    userViewModel.updateProfile(name, phone, selectedStatus, biography, "")
                    setResult(RESULT_CODE)
                    finish()
                } else if (imageUri != null) {
                    if (oldProfileImageUrl.isNotEmpty()) {
                        // Update Foto Profil Baru
                        userViewModel.updateProfile(name, phone, selectedStatus, biography, "")

                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            userViewModel.updateProfile(name, phone, selectedStatus, biography, imageUrl)
                            deleteProfilePhoto()
                            setResult(RESULT_CODE)
                            finish()
                        }
                    } else {
                        // Upload Foto Profil Baru
                        uploadImageToSupabase(imageUri!!) { imageUrl ->
                            userViewModel.updateProfile(name, phone, selectedStatus, biography, imageUrl)
                            setResult(RESULT_CODE)
                            finish()
                        }
                    }
                } else {
                    // Tidak ada foto yang diubah
                    userViewModel.updateProfile(name, phone, selectedStatus, biography, oldProfileImageUrl)
                    setResult(RESULT_CODE)
                    finish()
                }
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userImageUrl.observe(this@EditProfileActivity) { updateProfilePicture(it) }
            userName.observe(this@EditProfileActivity) { binding.tietFullName.setText(it) }
            userPhone.observe(this@EditProfileActivity) { phone ->
                val currentCode = binding.ccp.selectedCountryCode
                val phoneWithoutCode = phone?.removePrefix(currentCode)
                binding.tietPhoneNumber.setText(phoneWithoutCode)
            }
            userBiography.observe(this@EditProfileActivity) { binding.tietBiography.setText(it) }
            userStatus.observe(this@EditProfileActivity) { status ->
                val formattedStatus = status?.lowercase()?.replace("_", " ")?.capitalizeWords()
                if (statusList.contains(formattedStatus)) {
                    binding.spinnerStatus.setText(formattedStatus, false)
                }
            }
        }

        storageViewModel.apply {
            uploadStatus.observe(this@EditProfileActivity) { if (it) showToast("Foto profil berhasil diunggah") }
            deleteStatus.observe(this@EditProfileActivity) { if (it && isPhotoDeleted) showToast("Foto profil berhasil dihapus") }
            imageUrl.observeOnce(this@EditProfileActivity) { newImageUrl ->
                if (!newImageUrl.isNullOrEmpty()) {
                    userViewModel.updateProfileImageUrl(newImageUrl)
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateProfilePicture(imageUrl: String?) {
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun deleteProfilePhoto() {
        val currentImageUrl = userViewModel.userImageUrl.value
        if (!currentImageUrl.isNullOrEmpty()) {
            val fileName = currentImageUrl.substringAfterLast("/")
            storageViewModel.deleteFile(fileName, "profile_pictures")
        } else {
            showToast("Foto profil tidak ditemukan")
        }
    }

    private fun uploadImageToSupabase(uri: Uri, onSuccess: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            showToast("Gagal membaca file")
            return
        }

        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        binding.progressBar.visibility = View.VISIBLE

        val byteArray = inputStream.use { it.readBytes() }

        storageViewModel.uploadFile(byteArray, "profile_pictures", fileName)

        saveToFirestore(onSuccess)
    }

    private fun saveToFirestore(onSuccess: (String) -> Unit) {
        storageViewModel.imageUrl.observeOnce(this) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                userViewModel.updateProfileImageUrl(imageUrl)
                binding.progressBar.visibility = View.GONE
                onSuccess(imageUrl)
            }
        }
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}