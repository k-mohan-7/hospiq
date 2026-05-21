package com.simats.hospiq.network

import com.simats.hospiq.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("auth/login.php")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/register_patient.php")
    suspend fun registerPatient(@Body body: RegisterPatientRequest): Response<ApiResponse<AuthData>>

    @Multipart
    @POST("auth/register_doctor.php")
    suspend fun registerDoctor(
        @Part("full_name") fullName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("password") password: RequestBody,
        @Part("specialization") specialization: RequestBody,
        @Part("license_number") licenseNumber: RequestBody,
        @Part("years_experience") yearsExperience: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part("hospital_id") hospitalId: RequestBody,
        @Part("create_hospital") createHospital: RequestBody,
        @Part("hospital_name") hospitalName: RequestBody,
        @Part("hospital_address") hospitalAddress: RequestBody,
        @Part("hospital_city") hospitalCity: RequestBody,
        @Part("hospital_type") hospitalType: RequestBody,
        @Part profilePhoto: MultipartBody.Part? = null,
        @Part hospitalPhoto: MultipartBody.Part? = null
    ): Response<ApiResponse<AuthData>>

    @Multipart
    @POST("auth/update_profile.php")
    suspend fun updateProfile(
        @Part("user_id") userId: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part profilePhoto: MultipartBody.Part? = null
    ): Response<ApiResponse<AuthData>>

    // ── Hospitals ─────────────────────────────────────────────────────────────
    @GET("hospitals/get_all.php")
    suspend fun getAllHospitals(): Response<ApiResponse<HospitalListData>>

    @GET("hospitals/get_by_id.php")
    suspend fun getHospitalById(@Query("id") id: Int): Response<ApiResponse<Hospital>>

    @GET("hospitals/get_nearby.php")
    suspend fun getNearbyHospitals(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<ApiResponse<HospitalListData>>

    // ── Doctors ───────────────────────────────────────────────────────────────
    @GET("doctors/get_by_hospital.php")
    suspend fun getDoctorsByHospital(@Query("hospital_id") hospitalId: Int): Response<ApiResponse<DoctorListData>>

    @GET("doctors/get_profile.php")
    suspend fun getDoctorProfile(@Query("doctor_id") doctorId: Int): Response<ApiResponse<Doctor>>

    @POST("doctors/update_status.php")
    suspend fun updateDoctorStatus(@Body body: DoctorStatusRequest): Response<ApiResponse<Unit>>

    // ── Slots ─────────────────────────────────────────────────────────────────
    @GET("slots/get_available.php")
    suspend fun getAvailableSlots(
        @Query("doctor_id") doctorId: Int,
        @Query("date") date: String
    ): Response<ApiResponse<SlotListData>>

    // ── Appointments ──────────────────────────────────────────────────────────
    @POST("appointments/book.php")
    suspend fun bookAppointment(@Body body: BookAppointmentRequest): Response<ApiResponse<BookAppointmentData>>

    @GET("appointments/get_patient.php")
    suspend fun getPatientAppointments(@Query("patient_id") patientId: Int): Response<ApiResponse<AppointmentListData>>

    @GET("appointments/get_doctor.php")
    suspend fun getDoctorAppointments(@Query("doctor_id") doctorId: Int): Response<ApiResponse<AppointmentListData>>

    @POST("appointments/accept.php")
    suspend fun acceptAppointment(@Body body: AppointmentActionRequest): Response<ApiResponse<Unit>>

    @POST("appointments/reject.php")
    suspend fun rejectAppointment(@Body body: AppointmentActionRequest): Response<ApiResponse<Unit>>

    @POST("appointments/reschedule.php")
    suspend fun rescheduleAppointment(@Body body: RescheduleRequest): Response<ApiResponse<Unit>>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notifications/get.php")
    suspend fun getNotifications(@Query("user_id") userId: Int): Response<ApiResponse<NotificationListData>>
}
