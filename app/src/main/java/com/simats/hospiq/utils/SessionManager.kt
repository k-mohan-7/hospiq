package com.simats.hospiq.utils

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("hospiq_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String, userId: Int, role: String, name: String, hospitalId: Int? = null, phone: String? = null, profilePhoto: String? = null, doctorId: Int? = null) {
        prefs.edit()
            .putString("auth_token", token)
            .putInt("user_id", userId)
            .putString("user_role", role)
            .putString("user_name", name)
            .putString("user_phone", phone)
            .putString("profile_photo", profilePhoto)
            .putInt("hospital_id", hospitalId ?: -1)
            .putInt("doctor_id", doctorId ?: -1)
            .apply()
    }

    fun updateNameAndPhoneAndPhoto(name: String, phone: String, profilePhoto: String?) {
        prefs.edit()
            .putString("user_name", name)
            .putString("user_phone", phone)
            .putString("profile_photo", profilePhoto)
            .apply()
    }

    fun getToken(): String? = prefs.getString("auth_token", null)
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getRole(): String? = prefs.getString("user_role", null)
    fun getName(): String? = prefs.getString("user_name", null)
    fun getPhone(): String? = prefs.getString("user_phone", null)
    fun getProfilePhoto(): String? = prefs.getString("profile_photo", null)
    fun getHospitalId(): Int? {
        val id = prefs.getInt("hospital_id", -1)
        return if (id == -1) null else id
    }
    fun getDoctorId(): Int? {
        val id = prefs.getInt("doctor_id", -1)
        return if (id == -1) null else id
    }
    fun isLoggedIn(): Boolean = getToken() != null
    fun clearSession() = prefs.edit().clear().apply()

    fun getInitials(): String {
        val name = getName() ?: return "?"
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else {
            name.take(2).uppercase()
        }
    }

    // ── Notification Preferences ──────────────────────────────────────────────
    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notif_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean("notif_enabled", enabled).apply()

    fun getNotificationReminderMinutes(): Int = prefs.getInt("notif_reminder_minutes", 30)
    fun setNotificationReminderMinutes(minutes: Int) = prefs.edit().putInt("notif_reminder_minutes", minutes).apply()

    fun isAppointmentNotifEnabled(): Boolean = prefs.getBoolean("notif_appointment", true)
    fun setAppointmentNotifEnabled(enabled: Boolean) = prefs.edit().putBoolean("notif_appointment", enabled).apply()

    fun isStatusNotifEnabled(): Boolean = prefs.getBoolean("notif_status", true)
    fun setStatusNotifEnabled(enabled: Boolean) = prefs.edit().putBoolean("notif_status", enabled).apply()

    // ── User Location Cache ───────────────────────────────────────────────────
    fun saveLastLocation(lat: Double, lng: Double) {
        prefs.edit().putFloat("last_lat", lat.toFloat()).putFloat("last_lng", lng.toFloat()).apply()
    }
    fun getLastLat(): Double = prefs.getFloat("last_lat", 0f).toDouble()
    fun getLastLng(): Double = prefs.getFloat("last_lng", 0f).toDouble()
}

