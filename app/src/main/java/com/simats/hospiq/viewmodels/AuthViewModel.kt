package com.simats.hospiq.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.LoginRequest
import com.simats.hospiq.network.models.RegisterPatientRequest
import com.simats.hospiq.utils.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(
        email: String,
        password: String,
        onSuccess: (token: String, userId: Int, role: String, name: String, hospitalId: Int?, phone: String?, profilePhoto: String?, doctorId: Int?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter email and password"; return
        }
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    onSuccess(data.token ?: "", data.user_id, data.role, data.name, data.hospital_id, data.phone, data.profile_photo, data.doctor_id)
                } else {
                    errorMessage = response.body()?.message ?: "Login failed. Check credentials."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally { isLoading = false }
        }
    }

    fun registerPatient(
        fullName: String, email: String, phone: String, password: String,
        onSuccess: (token: String, userId: Int, role: String, name: String, hospitalId: Int?, phone: String?, profilePhoto: String?, doctorId: Int?) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                val response = RetrofitInstance.api.registerPatient(
                    RegisterPatientRequest(fullName, email, phone, password)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    onSuccess(data.token ?: "", data.user_id, data.role, data.name, data.hospital_id, data.phone, data.profile_photo, data.doctor_id)
                } else {
                    errorMessage = response.body()?.message ?: "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally { isLoading = false }
        }
    }

    fun registerDoctor(
        fullName: String, email: String, phone: String, password: String,
        specialization: String, licenseNumber: String, yearsExp: Int, bio: String,
        languages: String,
        hospitalId: Int,
        createHospital: Boolean,
        hospitalName: String,
        hospitalAddress: String,
        hospitalCity: String,
        hospitalType: String,
        hospitalLatitude: Double?,
        hospitalLongitude: Double?,
        profilePhotoBytes: ByteArray?,
        profilePhotoName: String?,
        hospitalPhotoBytes: ByteArray?,
        hospitalPhotoName: String?,
        onSuccess: (token: String, userId: Int, role: String, name: String, hospitalId: Int?, phone: String?, profilePhoto: String?, doctorId: Int?) -> Unit
    ) {
        fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                val profilePhotoPart = if (profilePhotoBytes != null && profilePhotoName != null) {
                    val requestFile = profilePhotoBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, profilePhotoBytes.size)
                    MultipartBody.Part.createFormData("profile_photo", profilePhotoName, requestFile)
                } else null

                val hospitalPhotoPart = if (hospitalPhotoBytes != null && hospitalPhotoName != null) {
                    val requestFile = hospitalPhotoBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, hospitalPhotoBytes.size)
                    MultipartBody.Part.createFormData("hospital_photo", hospitalPhotoName, requestFile)
                } else null

                val response = RetrofitInstance.api.registerDoctor(
                    fullName = fullName.toBody(),
                    email = email.toBody(),
                    phone = phone.toBody(),
                    password = password.toBody(),
                    specialization = specialization.toBody(),
                    licenseNumber = licenseNumber.toBody(),
                    yearsExperience = yearsExp.toString().toBody(),
                    bio = bio.toBody(),
                    hospitalId = hospitalId.toString().toBody(),
                    createHospital = (if (createHospital) "1" else "0").toBody(),
                    hospitalName = hospitalName.toBody(),
                    hospitalAddress = hospitalAddress.toBody(),
                    hospitalCity = hospitalCity.toBody(),
                    hospitalType = hospitalType.toBody(),
                    languages = languages.toBody(),
                    hospitalLatitude = (hospitalLatitude?.toString() ?: "0.0").toBody(),
                    hospitalLongitude = (hospitalLongitude?.toString() ?: "0.0").toBody(),
                    profilePhoto = profilePhotoPart,
                    hospitalPhoto = hospitalPhotoPart
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    onSuccess(data.token ?: "", data.user_id, data.role, data.name, data.hospital_id, data.phone, data.profile_photo, data.doctor_id)
                } else {
                    errorMessage = response.body()?.message ?: "Registration failed"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally { isLoading = false }
        }
    }

    fun updateProfile(
        userId: Int,
        fullName: String,
        phone: String,
        photoBytes: ByteArray?,
        photoFileName: String?,
        sessionManager: SessionManager,
        onSuccess: () -> Unit
    ) {
        fun String.toBody() = toRequestBody("text/plain".toMediaTypeOrNull())
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userIdBody = userId.toString().toBody()
                val fullNameBody = fullName.toBody()
                val phoneBody = phone.toBody()
                val profilePhotoPart = if (photoBytes != null && photoFileName != null) {
                    val requestFile = photoBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, photoBytes.size)
                    MultipartBody.Part.createFormData("profile_photo", photoFileName, requestFile)
                } else {
                    null
                }

                val response = RetrofitInstance.api.updateProfile(
                    userId = userIdBody,
                    fullName = fullNameBody,
                    phone = phoneBody,
                    profilePhoto = profilePhotoPart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!
                    sessionManager.updateNameAndPhoneAndPhoto(
                        name = data.name,
                        phone = data.phone ?: phone,
                        profilePhoto = data.profile_photo
                    )
                    onSuccess()
                } else {
                    errorMessage = response.body()?.message ?: "Profile update failed"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() { errorMessage = null }
}
