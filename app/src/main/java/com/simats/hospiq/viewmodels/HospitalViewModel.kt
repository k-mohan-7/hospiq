package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.Hospital
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HospitalUiState {
    object Loading : HospitalUiState()
    data class Success(
        val nearbyHospitals: List<Hospital>,
        val topRatedHospitals: List<Hospital>
    ) : HospitalUiState()
    data class Error(val message: String) : HospitalUiState()
}

sealed class HospitalDetailState {
    object Loading : HospitalDetailState()
    data class Success(val hospital: Hospital, val doctors: List<Doctor>) : HospitalDetailState()
    data class Error(val message: String) : HospitalDetailState()
}

class HospitalViewModel : ViewModel() {
    private val _listState = MutableStateFlow<HospitalUiState>(HospitalUiState.Loading)
    val listState: StateFlow<HospitalUiState> = _listState

    private val _detailState = MutableStateFlow<HospitalDetailState>(HospitalDetailState.Loading)
    val detailState: StateFlow<HospitalDetailState> = _detailState

    fun loadHospitals(lat: Double? = null, lng: Double? = null) {
        if (lat != null && lng != null) {
            loadNearbyHospitals(lat, lng)
            return
        }
        viewModelScope.launch {
            _listState.value = HospitalUiState.Loading
            try {
                val response = RetrofitInstance.api.getAllHospitals()
                if (response.isSuccessful && response.body()?.success == true) {
                    val hospitals = response.body()!!.data?.hospitals ?: emptyList()
                    _listState.value = HospitalUiState.Success(
                        nearbyHospitals = hospitals.take(3),
                        topRatedHospitals = hospitals.sortedByDescending { it.avgRating }
                    )
                } else {
                    _listState.value = HospitalUiState.Error(
                        response.body()?.message ?: "Failed to load hospitals"
                    )
                }
            } catch (e: Exception) {
                _listState.value = HospitalUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun loadNearbyHospitals(lat: Double, lng: Double) {
        viewModelScope.launch {
            _listState.value = HospitalUiState.Loading
            try {
                // get_nearby.php now falls back to all hospitals with computed distance when none are within radius
                val nearbyResponse = RetrofitInstance.api.getNearbyHospitals(lat, lng)
                val allResponse = RetrofitInstance.api.getAllHospitals(lat = lat, lng = lng)
                val nearby = if (nearbyResponse.isSuccessful && nearbyResponse.body()?.success == true) {
                    nearbyResponse.body()!!.data?.hospitals ?: emptyList()
                } else {
                    emptyList()
                }
                val all = if (allResponse.isSuccessful && allResponse.body()?.success == true) {
                    allResponse.body()!!.data?.hospitals ?: emptyList()
                } else {
                    emptyList()
                }
                _listState.value = HospitalUiState.Success(
                    nearbyHospitals = if (nearby.isNotEmpty()) nearby else all,
                    topRatedHospitals = all.sortedByDescending { it.avgRating }
                )
            } catch (e: Exception) {
                _listState.value = HospitalUiState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun loadHospitalDetail(hospitalId: Int, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            _detailState.value = HospitalDetailState.Loading
            try {
                val hospResponse = RetrofitInstance.api.getHospitalById(hospitalId, lat, lng)
                val docResponse = RetrofitInstance.api.getDoctorsByHospital(hospitalId)
                val hospital = hospResponse.body()?.data
                val doctors = docResponse.body()?.data?.doctors ?: emptyList()
                if (hospital != null) {
                    _detailState.value = HospitalDetailState.Success(hospital, doctors)
                } else {
                    _detailState.value = HospitalDetailState.Error("Hospital not found")
                }
            } catch (e: Exception) {
                _detailState.value = HospitalDetailState.Error(e.localizedMessage ?: "Error loading hospital")
            }
        }
    }
}
