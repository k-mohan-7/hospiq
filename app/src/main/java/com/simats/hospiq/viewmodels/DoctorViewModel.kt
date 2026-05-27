package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.DoctorPatient
import com.simats.hospiq.network.models.DoctorStatusRequest
import com.simats.hospiq.network.models.TimeSlot
import com.simats.hospiq.network.models.UpdateSlotsRequest
import com.simats.hospiq.network.models.CreateCustomSlotsRequest
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

    // Separate status state so Dashboard can read it without full profile reload
    private val _doctorStatus = MutableStateFlow("available")
    val doctorStatus: StateFlow<String> = _doctorStatus

    var statusUpdating = MutableStateFlow(false)

    fun loadDoctorProfile(doctorId: Int, date: String? = null) {
        viewModelScope.launch {
            _profileState.value = DoctorProfileState.Loading
            try {
                val selectedDate = date ?: LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val docResponse = RetrofitInstance.api.getDoctorProfile(doctorId)
                val slotResponse = RetrofitInstance.api.getAvailableSlots(doctorId, selectedDate)
                val doctor = docResponse.body()?.data
                val slots = slotResponse.body()?.data?.slots ?: emptyList()
                if (doctor != null) {
                    _doctorStatus.value = doctor.status
                    _profileState.value = DoctorProfileState.Success(doctor, slots)
                } else {
                    _profileState.value = DoctorProfileState.Error("Doctor not found")
                }
            } catch (e: Exception) {
                _profileState.value = DoctorProfileState.Error(e.localizedMessage ?: "Error loading profile")
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

    fun updateStatus(context: android.content.Context, doctorId: Int, status: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            statusUpdating.value = true
            try {
                RetrofitInstance.api.updateDoctorStatus(DoctorStatusRequest(doctorId, status))
                // Update local state immediately for responsive UI
                _doctorStatus.value = status
                // Also update profile state if loaded
                val current = _profileState.value
                if (current is DoctorProfileState.Success) {
                    _profileState.value = current.copy(doctor = current.doctor.copy(status = status))
                }
                
                val statusLabel = when (status) {
                    "available" -> "Available"
                    "busy" -> "Busy"
                    "in_surgery" -> "In Surgery"
                    else -> status
                }
                com.simats.hospiq.utils.NotificationService.showGeneralNotification(
                    context = context,
                    title = "⚡ Status Updated",
                    body = "Your status has been updated to: $statusLabel"
                )
            } catch (_: Exception) {}
            statusUpdating.value = false
            onDone()
        }
    }

    fun updateDoctorSlots(
        doctorId: Int,
        slots: List<Int>,
        dynamicTimings: Boolean,
        activeDates: List<String>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.updateDoctorSlots(
                    UpdateSlotsRequest(
                        doctorId = doctorId,
                        availableSlots = slots,
                        dynamicTimings = dynamicTimings
                    )
                )
            } catch (_: Exception) {}
            onDone()
        }
    }
    fun createCustomSlots(
        doctorId: Int,
        applyTo: String, // "all_days" or "specific_date"
        targetDate: String,
        timings: List<String>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.createCustomSlots(
                    CreateCustomSlotsRequest(
                        doctorId = doctorId,
                        applyTo = applyTo,
                        targetDate = targetDate,
                        timings = timings
                    )
                )
            } catch (_: Exception) {}
            onDone()
        }
    }

    // ── Doctor Patients ───────────────────────────────────────────────────────
    private val _doctorPatients = MutableStateFlow<List<DoctorPatient>>(emptyList())
    val doctorPatients: StateFlow<List<DoctorPatient>> = _doctorPatients

    fun loadDoctorPatients(doctorId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getDoctorPatients(doctorId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _doctorPatients.value = response.body()!!.data?.patients ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
