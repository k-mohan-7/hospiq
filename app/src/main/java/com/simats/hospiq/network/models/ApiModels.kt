package com.simats.hospiq.network.models

import com.google.gson.annotations.SerializedName

// ── Generic response wrapper ──────────────────────────────────────────────────
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

// ── Auth ──────────────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterPatientRequest(
    val full_name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class AuthData(
    val token: String? = null,
    val user_id: Int,
    val role: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val profile_photo: String? = null,
    val doctor_id: Int? = null,
    val hospital_id: Int? = null
)

// ── Hospital ──────────────────────────────────────────────────────────────────
data class Hospital(
    val id: Int = 0,
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val type: String = "",
    val photo: String? = null,
    @SerializedName("opening_hours") val openingHours: String = "9AM-9PM",
    @SerializedName("avg_rating") val avgRating: Float = 0f,
    @SerializedName("total_reviews") val totalReviews: Int = 0,
    val distance: Float = 0f,
    val facilities: List<String> = emptyList(),
    val specialties: List<String> = emptyList(),
    @SerializedName("doctor_count") val doctorCount: Int = 0
)

data class HospitalListData(val hospitals: List<Hospital>)

// ── Doctor ────────────────────────────────────────────────────────────────────
data class Doctor(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    val name: String = "",
    @SerializedName("hospital_id") val hospitalId: Int = 0,
    @SerializedName("hospital_name") val hospitalName: String = "",
    val specialization: String = "",
    val rating: Float = 0f,
    @SerializedName("total_patients") val totalPatients: Int = 0,
    @SerializedName("years_experience") val yearsExperience: Int = 0,
    val status: String = "available",
    val photo: String? = null,
    val bio: String = "",
    @SerializedName("dynamic_timings") val dynamicTimings: Boolean = true,
    @SerializedName("active_slots") val activeSlots: String = "1,2,3,4,5,6,7,8,9,10,11,12"
)

data class DoctorListData(val doctors: List<Doctor>)

data class DoctorStatusRequest(
    @SerializedName("doctor_id") val doctorId: Int,
    val status: String
)

// ── Time Slot ─────────────────────────────────────────────────────────────────
data class TimeSlot(
    val id: Int = 0,
    @SerializedName("doctor_id") val doctorId: Int = 0,
    @SerializedName("slot_date") val slotDate: String = "",
    @SerializedName("slot_time") val slotTime: String = "",
    @SerializedName("is_booked") val isBooked: Boolean = false
)

data class SlotListData(val slots: List<TimeSlot>)

// ── Appointment ───────────────────────────────────────────────────────────────
data class Appointment(
    val id: Int = 0,
    @SerializedName("patient_id") val patientId: Int = 0,
    @SerializedName("doctor_id") val doctorId: Int = 0,
    @SerializedName("hospital_id") val hospitalId: Int = 0,
    @SerializedName("doctor_name") val doctorName: String = "",
    @SerializedName("patient_name") val patientName: String = "",
    val specialization: String = "",
    @SerializedName("hospital_name") val hospitalName: String = "",
    @SerializedName("appointment_date") val date: String = "",
    @SerializedName("appointment_time") val time: String = "",
    val status: String = "pending",
    @SerializedName("consultation_type") val consultationType: String = "in_person",
    @SerializedName("illness_name") val illnessName: String? = null,
    @SerializedName("illness_description") val illnessDescription: String? = null,
    @SerializedName("precautions") val precautions: String? = null,
    @SerializedName("doctor_advice") val doctorAdvice: String? = null,
    @SerializedName("doctor_status") val doctorStatus: String? = "available"
)

data class AppointmentListData(val appointments: List<Appointment>)

data class BookAppointmentRequest(
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("doctor_id") val doctorId: Int,
    @SerializedName("slot_id") val slotId: Int,
    @SerializedName("consultation_type") val consultationType: String,
    @SerializedName("illness_name") val illnessName: String = "",
    @SerializedName("illness_description") val illnessDescription: String = "",
    @SerializedName("precautions") val precautions: String = ""
)

data class BookAppointmentData(
    @SerializedName("appointment_id") val appointmentId: Int
)

data class AppointmentActionRequest(
    @SerializedName("appointment_id") val appointmentId: Int
)

data class RescheduleRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("new_date") val newDate: String,
    @SerializedName("new_time") val newTime: String,
    val reason: String = ""
)

data class SubmitAdviceRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("doctor_advice") val doctorAdvice: String
)

data class UpdateSlotsRequest(
    @SerializedName("doctor_id") val doctorId: Int,
    @SerializedName("available_slots") val availableSlots: List<Int>,
    @SerializedName("dynamic_timings") val dynamicTimings: Boolean
)

data class CreateCustomSlotsRequest(
    @SerializedName("doctor_id") val doctorId: Int,
    @SerializedName("apply_to") val applyTo: String,
    @SerializedName("target_date") val targetDate: String,
    @SerializedName("timings") val timings: List<String>
)

// ── Notification ──────────────────────────────────────────────────────────────
data class AppNotification(
    val id: Int = 0,
    val title: String = "",
    val body: String = "",
    val type: String = "general",
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("time_ago") val timeAgo: String = ""
)

data class NotificationListData(val notifications: List<AppNotification>)

// ── User (Session) ────────────────────────────────────────────────────────────
data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "patient",
    val hospitalId: Int? = null,
    val hospitalName: String? = null,
    val specialization: String? = null
)
