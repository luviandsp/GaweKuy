package com.gawebersama.gawekuy.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.dataclass.ServiceModel
import com.gawebersama.gawekuy.data.dataclass.ServiceSelectionModel
import com.gawebersama.gawekuy.data.repository.ServiceRepository
import kotlinx.coroutines.launch

class ServiceViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()

    companion object {
        private const val TAG = "ServiceViewModel"
    }

    private val _services = MutableLiveData<List<ServiceModel>>(emptyList())
    val services: LiveData<List<ServiceModel>> get() = _services

    private val _imageBannerUrl = MutableLiveData<String?>()
    val imageBannerUrl: LiveData<String?> get() = _imageBannerUrl

    private val _serviceName = MutableLiveData<String?>()
    val serviceName: LiveData<String?> get() = _serviceName

    private val _serviceDesc = MutableLiveData<String?>()
    val serviceDesc: LiveData<String?> get() = _serviceDesc

    private val _serviceTypes = MutableLiveData<List<ServiceSelectionModel>?>()
    val serviceTypes: LiveData<List<ServiceSelectionModel>?> get() = _serviceTypes

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    init {
        fetchUserServices()
    }

    // Buat jasa baru
    fun createService(serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceTypes: List<ServiceSelectionModel>) {
        viewModelScope.launch {
            val result = serviceRepository.createService(serviceName, serviceDesc, imageBannerUrl, serviceTypes)
            if (result.first) {
                _operationResult.postValue(result)
                _serviceName.postValue(serviceName)
                _serviceDesc.postValue(serviceDesc)
                _imageBannerUrl.postValue(imageBannerUrl)
                _serviceTypes.postValue(serviceTypes)
            }
        }
    }

    // Ambil semua jasa user
    fun fetchUserServices() {
        viewModelScope.launch {
            val services = serviceRepository.getUserServices()

            Log.d(TAG, "Fetched Services: $services") // Debug seluruh data

            _services.postValue(services)
        }
    }

    fun fetchServiceById(serviceId: String) {
        viewModelScope.launch {
            val service = serviceRepository.getServiceById(serviceId)

            if (service != null) {
                _imageBannerUrl.postValue(service.imageBannerUrl)
                _serviceName.postValue(service.serviceName)
                _serviceDesc.postValue(service.serviceDesc)
                _serviceTypes.postValue(service.serviceTypes)
            }
            Log.d(TAG, "Fetched Service: $service") // Debug seluruh data
        }
    }

    // Update jasa berdasarkan serviceId
    fun updateService(serviceId: String, serviceName: String, serviceDesc: String, imageBannerUrl: String, serviceTypes: List<ServiceSelectionModel>) {
        viewModelScope.launch {
            val result = serviceRepository.updateService(serviceId, serviceName, serviceDesc, imageBannerUrl, serviceTypes)
            _operationResult.postValue(result)
            if (result.first) {
                _serviceName.postValue(serviceName)
                _serviceDesc.postValue(serviceDesc)
                _imageBannerUrl.postValue(imageBannerUrl)
                _serviceTypes.postValue(serviceTypes)
            }
        }
    }

    // Update hanya URL gambar jasa
    fun updateServiceImageUrl(serviceId: String, imageUrl: String) {
        viewModelScope.launch {
            val result = serviceRepository.updateServiceImageUrl(serviceId, imageUrl)
            _operationResult.postValue(result)
            if (result.first) {
                _imageBannerUrl.postValue(imageUrl)
            }
        }
    }

    // Hapus jasa berdasarkan serviceId
    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            val result = serviceRepository.deleteService(serviceId)
            _operationResult.postValue(result)
            if (result.first) {
                _serviceName.postValue(null)
                _serviceDesc.postValue(null)
                _imageBannerUrl.postValue(null)
                _serviceTypes.postValue(null)
            }
        }
    }
}
