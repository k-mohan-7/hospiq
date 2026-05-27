package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.network.models.AppointmentActionRequest
import com.simats.hospiq.network.models.BookAppointmentRequest
import com.simats.hospiq.network.models.RescheduleRequest
import com.simats.hospiq.network.models.SubmitAdviceRequest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class AppointmentListState {
    object Loading : AppointmentListState()
    data class Success(val appointments: List<Appointment>) : AppointmentListState()
    data class Error(val message: String) : AppointmentListState()
}

sealed class BookingState {
    object Idle : BookingState()
    object Loading : BookingState()
    data class Success(val appointmentId: Int) : BookingState()
    data class Error(val message: String) : BookingState()
}

class AppointmentViewModel : ViewModel() {
    private val _appointmentsState = MutableStateFlow<AppointmentListState>(AppointmentListState.Loading)
    val appointmentsState: StateFlow<AppointmentListState> = _appointmentsState

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState

    fun loadPatientAppointments(patientId: Int) {
        viewModelScope.launch {
            _appointmentsState.value = AppointmentListState.Loading
            try {
                val response = RetrofitInstance.api.getPatientAppointments(patientId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _appointmentsState.value = AppointmentListState.Success(
                        response.body()!!.data?.appointments ?: emptyList()
                    )
                } else {
                    _appointmentsState.value = AppointmentListState.Error(
                        response.body()?.message ?: "Failed to load appointments"
                    )
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentListState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun loadDoctorAppointments(doctorId: Int) {
        viewModelScope.launch {
            _appointmentsState.value = AppointmentListState.Loading
            try {
                val response = RetrofitInstance.api.getDoctorAppointments(doctorId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _appointmentsState.value = AppointmentListState.Success(
                        response.body()!!.data?.appointments ?: emptyList()
                    )
                } else {
                    _appointmentsState.value = AppointmentListState.Error(
                        response.body()?.message ?: "Failed to load appointments"
                    )
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentListState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun bookAppointment(
        patientId: Int, doctorId: Int, slotId: Int, consultationType: String,
        date: String = "",
        illnessName: String = "", illnessDescription: String = "", precautions: String = ""
    ) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            try {
                val response = RetrofitInstance.api.bookAppointment(
                    BookAppointmentRequest(
                        patientId = patientId,
                        doctorId = doctorId,
                        slotId = slotId,
                        consultationType = consultationType,
                        illnessName = illnessName,
                        illnessDescription = illnessDescription,
                        precautions = precautions
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val apptId = response.body()!!.data?.appointmentId ?: 0
                    _bookingState.value = BookingState.Success(apptId)
                } else {
                    _bookingState.value = BookingState.Error(
                        response.body()?.message ?: "Booking failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error(
                    e.localizedMessage ?: "Network error. Please check your connection."
                )
            }
        }
    }

    fun acceptAppointment(context: android.content.Context, appointmentId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.acceptAppointment(AppointmentActionRequest(appointmentId))
                if (res.isSuccessful && res.body()?.success == true) {
                    com.simats.hospiq.utils.NotificationService.showAppointmentNotification(
                        context = context,
                        title = "✅ Appointment Accepted",
                        body = "The doctor has accepted your appointment request."
                    )
                }
            } catch (_: Exception) {}
            onDone()
        }
    }

    fun rejectAppointment(context: android.content.Context, appointmentId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.rejectAppointment(AppointmentActionRequest(appointmentId))
                if (res.isSuccessful && res.body()?.success == true) {
                    com.simats.hospiq.utils.NotificationService.showAppointmentNotification(
                        context = context,
                        title = "❌ Appointment Rejected",
                        body = "Your appointment request has been declined."
                    )
                }
            } catch (_: Exception) {}
            onDone()
        }
    }

    fun rescheduleAppointment(appointmentId: Int, newDate: String, newTime: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.rescheduleAppointment(RescheduleRequest(appointmentId, newDate, newTime))
            } catch (_: Exception) {}
            onDone()
        }
    }

    fun cancelAppointment(appointmentId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.cancelAppointment(AppointmentActionRequest(appointmentId))
            } catch (_: Exception) {}
            onDone()
        }
    }

    fun submitDoctorAdvice(appointmentId: Int, advice: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.submitDoctorAdvice(SubmitAdviceRequest(appointmentId, advice))
            } catch (_: Exception) {}
            onDone()
        }
    }

    private val _healthReports = MutableStateFlow<List<com.simats.hospiq.network.models.HealthReport>>(emptyList())
    val healthReports: StateFlow<List<com.simats.hospiq.network.models.HealthReport>> = _healthReports

    fun loadPatientHealthReports(patientId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getHealthReports(patientId = patientId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _healthReports.value = response.body()!!.data?.reports ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitHealthReport(
        appointmentId: Int,
        patientId: Int,
        doctorId: Int,
        healthStatus: String,
        notes: String,
        documentBytesList: List<ByteArray>,
        documentNames: List<String>,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
                
                val docParts = documentBytesList.mapIndexed { idx, bytes ->
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)
                    val fileName = documentNames.getOrNull(idx) ?: "doc_${System.currentTimeMillis()}_$idx.png"
                    MultipartBody.Part.createFormData("documents[]", fileName, requestFile)
                }

                val res = RetrofitInstance.api.submitHealthReport(
                    appointmentId = appointmentId.toString().toBody(),
                    patientId = patientId.toString().toBody(),
                    doctorId = doctorId.toString().toBody(),
                    healthStatus = healthStatus.toBody(),
                    notes = notes.toBody(),
                    documents = docParts
                )
                if (res.isSuccessful) {
                    loadPatientHealthReports(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onDone()
        }
    }

    fun editHealthReport(
        reportId: Int,
        patientId: Int,
        healthStatus: String,
        notes: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.editHealthReport(reportId, healthStatus, notes)
                if (res.isSuccessful && res.body()?.success == true) {
                    loadPatientHealthReports(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace();
            }
            onDone()
        }
    }

    fun deleteHealthReport(
        reportId: Int,
        patientId: Int,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.api.deleteHealthReport(reportId)
                if (res.isSuccessful && res.body()?.success == true) {
                    loadPatientHealthReports(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onDone()
        }
    }

    fun rateHospital(hospitalId: Int, patientId: Int, rating: Int, review: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.rateHospital(
                    com.simats.hospiq.network.models.RateHospitalRequest(
                        hospitalId = hospitalId,
                        patientId = patientId,
                        rating = rating,
                        review = review
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onDone()
        }
    }

    fun resetBooking() { _bookingState.value = BookingState.Idle }
}
