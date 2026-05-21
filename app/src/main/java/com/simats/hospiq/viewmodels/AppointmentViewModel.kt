package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.network.models.AppointmentActionRequest
import com.simats.hospiq.network.models.BookAppointmentRequest
import com.simats.hospiq.network.models.RescheduleRequest
import com.simats.hospiq.utils.DemoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
                    _appointmentsState.value = AppointmentListState.Success(DemoData.patientAppointments)
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentListState.Success(DemoData.patientAppointments)
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
                    _appointmentsState.value = AppointmentListState.Success(DemoData.doctorAppointments)
                }
            } catch (e: Exception) {
                _appointmentsState.value = AppointmentListState.Success(DemoData.doctorAppointments)
            }
        }
    }

    fun bookAppointment(
        patientId: Int, doctorId: Int, slotId: Int, consultationType: String
    ) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            try {
                val response = RetrofitInstance.api.bookAppointment(
                    BookAppointmentRequest(patientId, doctorId, slotId, consultationType)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val apptId = response.body()!!.data?.appointmentId ?: 0
                    _bookingState.value = BookingState.Success(apptId)
                } else {
                    _bookingState.value = BookingState.Error(response.body()?.message ?: "Booking failed")
                }
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun acceptAppointment(appointmentId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try { RetrofitInstance.api.acceptAppointment(AppointmentActionRequest(appointmentId)) }
            catch (_: Exception) {}
            onDone()
        }
    }

    fun rejectAppointment(appointmentId: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try { RetrofitInstance.api.rejectAppointment(AppointmentActionRequest(appointmentId)) }
            catch (_: Exception) {}
            onDone()
        }
    }

    fun rescheduleAppointment(appointmentId: Int, newDate: String, newTime: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try { RetrofitInstance.api.rescheduleAppointment(RescheduleRequest(appointmentId, newDate, newTime)) }
            catch (_: Exception) {}
            onDone()
        }
    }

    fun resetBooking() { _bookingState.value = BookingState.Idle }
}
