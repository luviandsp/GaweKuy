package com.gawebersama.gawekuy.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.repository.StorageRepository
import kotlinx.coroutines.launch
import java.io.File

class StorageViewModel : ViewModel() {

    private val storageRepository = StorageRepository()

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> get() = _imageUrl

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    fun uploadImage(imageFile: File, folderName : String, fileName: String) {
        viewModelScope.launch {
            val url = storageRepository.uploadImageToSupabase(imageFile, folderName, fileName)
            if (url != null) {
                _imageUrl.postValue(url)
                _uploadStatus.postValue(true)
            } else {
                _uploadStatus.postValue(false)
            }
        }
    }

    fun saveImageUrlToFirestore(imageUrl: String) {
        viewModelScope.launch {
            val isSuccess = storageRepository.saveImageUrlToFirestore(imageUrl)
            if (isSuccess) {
                _imageUrl.postValue(imageUrl)
            }
        }
    }
}