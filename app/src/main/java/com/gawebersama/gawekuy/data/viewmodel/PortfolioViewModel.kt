package com.gawebersama.gawekuy.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gawebersama.gawekuy.data.datamodel.PortfolioModel
import com.gawebersama.gawekuy.data.repository.PortfolioRepository
import kotlinx.coroutines.launch

class PortfolioViewModel : ViewModel() {

    private val portfolioRepository = PortfolioRepository()

    companion object {
        private const val TAG = "PortfolioViewModel"
    }
    private val _portfolios = MutableLiveData<List<PortfolioModel>>()
    val portfolios: LiveData<List<PortfolioModel>> get() = _portfolios

    private val _imageBannerUrl = MutableLiveData<String?>()
    val imageBannerUrl: LiveData<String?> get() = _imageBannerUrl

    private val _portfolioName = MutableLiveData<String?>()
    val portfolioName: LiveData<String?> get() = _portfolioName

    private val _portfolioDesc = MutableLiveData<String?>()
    val portfolioDesc: LiveData<String?> get() = _portfolioDesc

//    private val _portfolioImageUrl = MutableLiveData<List<String>>()
//    val portfolioImageUrl: LiveData<List<String>> get() = _portfolioImageUrl

    private val _operationResult = MutableLiveData<Pair<Boolean, String?>>()
    val operationResult: LiveData<Pair<Boolean, String?>> get() = _operationResult

    fun createPortfolio(
        portfolioName: String,
        portfolioDesc: String,
        portfolioBannerImage : String,
//        portfolioImageUrl: List<String>
    ) {
        viewModelScope.launch {
            val result = portfolioRepository.createPortfolio(
                portfolioTitle = portfolioName,
                portfolioDesc = portfolioDesc,
                portfolioBannerImage = portfolioBannerImage
            )

            if (result.first) {
                _operationResult.postValue(result)
                _portfolioName.postValue(portfolioName)
                _portfolioDesc.postValue(portfolioDesc)
                _imageBannerUrl.postValue(portfolioBannerImage)
//                _portfolioImageUrl.postValue(portfolioImageUrl)

                Log.d(TAG, "Portfolio created with ID: ${result.second}")
            }
        }
    }

    fun fetchUserPortfolios(userId: String) {
        viewModelScope.launch {
            val portfolios = portfolioRepository.getUserPortfolios(userId)
            _portfolios.postValue(portfolios)
        }
    }

    fun fetchPortfolioById(portfolioId: String) {
        viewModelScope.launch {
            val portfolio = portfolioRepository.getPortfolioById(portfolioId)

            if (portfolio != null) {
                _portfolioName.postValue(portfolio.portfolioTitle)
                _portfolioDesc.postValue(portfolio.portfolioDesc)
                _imageBannerUrl.postValue(portfolio.portfolioBannerImage)
//                _portfolioImageUrl.postValue(portfolio.portfolioImageUrl)

                Log.d(TAG, "Fetched Portfolio: $portfolio")
            } else {
                Log.e(TAG, "Portfolio not found: $portfolioId")
            }
        }
    }

    fun updatePortfolio(
        portfolioId: String,
        portfolioName: String,
        portfolioDesc: String,
        portfolioBannerImage : String,
//        portfolioImageUrl: List<String>
    ) {
        viewModelScope.launch {
            val result = portfolioRepository.updatePortfolio(
                portfolioId = portfolioId,
                portfolioTitle = portfolioName,
                portfolioDesc = portfolioDesc,
                portfolioBannerImage = portfolioBannerImage
            )

            _operationResult.postValue(result)

            if (result.first) {
                _portfolioName.postValue(portfolioName)
                _portfolioDesc.postValue(portfolioDesc)
                _imageBannerUrl.postValue(portfolioBannerImage)
//                _portfolioImageUrl.postValue(portfolioImageUrl)
            }

            Log.d(TAG, "Portfolio updated with ID: $portfolioId")
        }
    }

    fun deletePortfolio(portfolioId: String) {
        viewModelScope.launch {
            val result = portfolioRepository.deletePortfolio(portfolioId)
            _operationResult.postValue(result)
            if (result.first) {
                _portfolios.value = _portfolios.value?.filterNot { it.portfolioId == portfolioId }
            }
        }
    }
}