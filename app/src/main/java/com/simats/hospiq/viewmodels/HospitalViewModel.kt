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

    fun loadHospitals() {
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

    fun loadHospitalDetail(hospitalId: Int) {
        viewModelScope.launch {
            _detailState.value = HospitalDetailState.Loading
            try {
                val hospResponse = RetrofitInstance.api.getHospitalById(hospitalId)
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
