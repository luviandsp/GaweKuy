package com.gawebersama.gawekuy.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.datamodel.PortfolioModel
import com.gawebersama.gawekuy.data.datamodel.ServiceModel
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.data.datamodel.ServiceWithUserModel
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.gawebersama.gawekuy.data.repository.ServiceRepository
import kotlinx.coroutines.launch

class ServiceViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()

    companion object {
        private const val TAG = "ServiceViewModel"
    }

    private val _services = MutableLiveData<List<ServiceModel>?>()
    val services: LiveData<List<ServiceModel>?> get() = _services

    private val _serviceWithUser = MutableLiveData<List<ServiceWithUserModel>?>()
    val serviceWithUser: LiveData<List<ServiceWithUserModel>?> get() = _serviceWithUser

    private val _imageBannerUrl = MutableLiveData<String?>()
    val imageBannerUrl: LiveData<String?> get() = _imageBannerUrl

    private val _serviceName = MutableLiveData<String?>()
    val serviceName: LiveData<String?> get() = _serviceName

    private val _serviceDesc = MutableLiveData<String?>()
    val serviceDesc: LiveData<String?> get() = _serviceDesc

    private val _serviceCategory = MutableLiveData<String?>()
    val serviceCategory: LiveData<String?> get() = _serviceCategory

    private val _minPrice = MutableLiveData<Double?>()
    val minPrice: LiveData<Double?> get() = _minPrice

    private val _serviceTypes = MutableLiveData<List<ServiceSelectionModel>?>()
    val serviceTypes: LiveData<List<ServiceSelectionModel>?> get() = _serviceTypes

    private val _serviceTags = MutableLiveData<List<String>?>()
    val serviceTags: LiveData<List<String>?> get() = _serviceTags

    private val _servicePortofolio = MutableLiveData<List<Map<String, String>>>()
    val servicePortofolio: LiveData<List<Map<String, String>>> get() = _servicePortofolio

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    private val _ownerServiceId = MutableLiveData<String?>()
    val ownerServiceId: LiveData<String?> get() = _ownerServiceId

    private val _ownerServiceName = MutableLiveData<String?>()
    val ownerServiceName: LiveData<String?> get() = _ownerServiceName

    private val _ownerServicePhone = MutableLiveData<String?>()
    val ownerServicePhone: LiveData<String?> get() = _ownerServicePhone

    private val _ownerServiceImage = MutableLiveData<String?>()
    val ownerServiceImage: LiveData<String?> get() = _ownerServiceImage

    private val _ownerServiceAccountStatus = MutableLiveData<Boolean?>()
    val ownerServiceAccountStatus: LiveData<Boolean?> get() = _ownerServiceAccountStatus

    private val _ownerServiceBio = MutableLiveData<String?>()
    val ownerServiceBio: LiveData<String?> get() = _ownerServiceBio

    private val _ownerStatus = MutableLiveData<String?>()
    val ownerStatus: LiveData<String?> get() = _ownerStatus

    private val _selectedPortfolio = MutableLiveData<List<PortfolioModel>>()
    val selectedPortfolio: LiveData<List<PortfolioModel>> get() = _selectedPortfolio

    private var currentList = mutableListOf<ServiceWithUserModel>()

    fun hasMoreData(): Boolean {
        return !serviceRepository.isLastPage()
    }

    // Buat jasa baru
    fun createService(
        serviceName: String,
        serviceDesc: String,
        imageBannerUrl: String,
        serviceCategory: String,
        serviceTypes: List<ServiceSelectionModel>,
        serviceTags: List<String>,
        portfolio: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            val result = serviceRepository.createService(
                serviceName = serviceName,
                serviceDesc = serviceDesc,
                imageBannerUrl = imageBannerUrl,
                serviceCategory = serviceCategory,
                serviceTypes = serviceTypes,
                serviceTags = serviceTags,
                portfolio = portfolio
            )

            if (result.first) {
                _operationResult.postValue(result)
                _serviceName.postValue(serviceName)
                _serviceDesc.postValue(serviceDesc)
                _imageBannerUrl.postValue(imageBannerUrl)
                _serviceTypes.postValue(serviceTypes)
                _serviceCategory.postValue(serviceCategory)
                _minPrice.postValue(serviceTypes.minOfOrNull { it.price })
                _serviceTags.postValue(serviceTags)
                _servicePortofolio.postValue(portfolio)
            }
        }
    }

    fun searchService(filter: FilterAndOrderService?, query: String, resetPaging: Boolean = false) {
        viewModelScope.launch {
            val result = serviceRepository.searchService(filter, query, resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(result)
            _serviceWithUser.postValue(currentList)

            Log.d(TAG, "Query: $query, Result Count: ${result.size}")
        }
    }


    // Ambil semua jasa user
    fun fetchUserServices(resetPaging: Boolean = false) {
        viewModelScope.launch {
            val servicesWithUser = serviceRepository.getUserServices(resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(servicesWithUser)
            _serviceWithUser.postValue(currentList)
            Log.d(TAG, "Fetched Services: ${currentList.size}")
        }
    }

    // Ambil semua jasa
    fun fetchAllServices(filter: FilterAndOrderService?, resetPaging: Boolean = false) {
        viewModelScope.launch {
            val newServices = serviceRepository.getAllService(filter, resetPaging)

            if (resetPaging) {
                currentList.clear()
            }

            currentList.addAll(newServices) // Tambahkan data baru ke daftar lama
            _serviceWithUser.postValue(currentList) // Update UI
            Log.d(TAG, "Current list size: ${currentList.size}")
        }
    }

    // 🔹 Ambil jasa berdasarkan ID + data user
    fun fetchServiceById(serviceId: String) {
        viewModelScope.launch {
            val service = serviceRepository.getServiceById(serviceId)

            if (service != null) {
                _imageBannerUrl.postValue(service.service.imageBannerUrl)
                _serviceName.postValue(service.service.serviceName)
                _serviceDesc.postValue(service.service.serviceDesc)
                _serviceTypes.postValue(service.service.serviceTypes)
                _serviceCategory.postValue(service.service.serviceCategory)
                _minPrice.postValue(service.service.serviceTypes.minOfOrNull { it.price })
                _serviceTags.postValue(service.service.serviceTags)
                _ownerServiceId.postValue(service.user.userId)
                _ownerServiceName.postValue(service.user.name)
                _ownerServicePhone.postValue(service.user.phone)
                _ownerServiceImage.postValue(service.user.profileImageUrl)
                _ownerServiceAccountStatus.postValue(service.user.accountStatus)
                _ownerServiceBio.postValue(service.user.biography)
                _ownerStatus.postValue(service.user.userStatus)
                _servicePortofolio.postValue(service.service.portfolio)
                Log.d(TAG, "Fetched Service: $service")
            } else {
                Log.e(TAG, "Service not found: $serviceId")
            }
        }
    }

    fun fetchPortfolioByServiceId(serviceId: String) {
        viewModelScope.launch {
            val portfolio = serviceRepository.getPortfolioByServiceId(serviceId)
            _selectedPortfolio.postValue(portfolio)
        }
    }

    // Update jasa berdasarkan serviceId
    fun updateService(
        serviceId: String,
        serviceName: String,
        serviceDesc: String,
        imageBannerUrl: String,
        serviceCategory: String,
        serviceTypes: List<ServiceSelectionModel>,
        serviceTags: List<String>,
        portfolio: List<Map<String, String>>
    ) {
        viewModelScope.launch {
            val result = serviceRepository.updateService(
                serviceId = serviceId,
                serviceName = serviceName,
                serviceDesc = serviceDesc,
                imageBannerUrl = imageBannerUrl,
                serviceCategory = serviceCategory,
                serviceTypes = serviceTypes,
                serviceTags = serviceTags,
                portfolio = portfolio
            )

            _operationResult.postValue(result)

            if (result.first) {
                _serviceName.postValue(serviceName)
                _serviceDesc.postValue(serviceDesc)
                _imageBannerUrl.postValue(imageBannerUrl)
                _serviceTypes.postValue(serviceTypes)
                _serviceCategory.postValue(serviceCategory)
                _minPrice.postValue(serviceTypes.minOfOrNull { it.price })
                _serviceTags.postValue(serviceTags)
                _servicePortofolio.postValue(portfolio)
            }
        }
    }

    // Hapus jasa berdasarkan serviceId
    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            val result = serviceRepository.deleteService(
                serviceId = serviceId
            )

            _operationResult.postValue(result)

            if (result.first) {
                _services.value = _services.value?.filterNot { it.serviceId == serviceId }
            }
        }
    }
}
