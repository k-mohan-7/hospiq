package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.DoctorStatusRequest
import com.simats.hospiq.network.models.TimeSlot
import com.simats.hospiq.utils.DemoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class DoctorProfileState {
    object Loading : DoctorProfileState()
    data class Success(val doctor: Doctor, val slots: List<TimeSlot>) : DoctorProfileState()
    data class Error(val message: String) : DoctorProfileState()
}

class DoctorViewModel : ViewModel() {
    private val _profileState = MutableStateFlow<DoctorProfileState>(DoctorProfileState.Loading)
    val profileState: StateFlow<DoctorProfileState> = _profileState

    var statusUpdating = MutableStateFlow(false)

    fun loadDoctorProfile(doctorId: Int, date: String? = null) {
        viewModelScope.launch {
            _profileState.value = DoctorProfileState.Loading
            try {
                val selectedDate = date ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val docResponse = RetrofitInstance.api.getDoctorProfile(doctorId)
                val slotResponse = RetrofitInstance.api.getAvailableSlots(doctorId, selectedDate)
                val doctor = docResponse.body()?.data
                val slots = slotResponse.body()?.data?.slots ?: DemoData.timeSlots
                if (doctor != null) {
                    _profileState.value = DoctorProfileState.Success(doctor, slots)
                } else {
                    val fallback = DemoData.doctors.find { it.id == doctorId }
                    if (fallback != null) _profileState.value = DoctorProfileState.Success(fallback, DemoData.timeSlots)
                    else _profileState.value = DoctorProfileState.Error("Doctor not found")
                }
            } catch (e: Exception) {
                val fallback = DemoData.doctors.find { it.id == doctorId }
                if (fallback != null) _profileState.value = DoctorProfileState.Success(fallback, DemoData.timeSlots)
                else _profileState.value = DoctorProfileState.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun loadSlotsForDate(doctorId: Int, date: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is DoctorProfileState.Success) {
                try {
                    val response = RetrofitInstance.api.getAvailableSlots(doctorId, date)
                    val slots = response.body()?.data?.slots ?: currentState.slots
                    _profileState.value = currentState.copy(slots = slots)
                } catch (_: Exception) {}
            }
        }
    }

    fun updateStatus(doctorId: Int, status: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            statusUpdating.value = true
            try {
                RetrofitInstance.api.updateDoctorStatus(DoctorStatusRequest(doctorId, status))
            } catch (_: Exception) {}
            statusUpdating.value = false
            onDone()
        }
    }
}
