package com.simats.hospiq.navigation

sealed class Screen(val route: String) {
    object Splash               : Screen("splash")
    object Onboarding           : Screen("onboarding")
    object PatientLogin         : Screen("patient_login")
    object PatientSignUp        : Screen("patient_signup")
    object DoctorRegister       : Screen("doctor_register")
    object PatientHome          : Screen("patient_home")
    object HospitalDetail       : Screen("hospital_detail/{hospitalId}")
    object DoctorProfile        : Screen("doctor_profile/{doctorId}")
    object Booking              : Screen("booking/{doctorId}/{slotId}")
    object AppointmentConfirm   : Screen("appointment_confirm/{appointmentId}")
    object PatientAppointments  : Screen("patient_appointments")
    object PatientNotifications : Screen("patient_notifications")
    object Search               : Screen("search")
    object PatientProfile       : Screen("patient_profile")
    object DoctorDashboard      : Screen("doctor_dashboard")
    object DoctorAppointments   : Screen("doctor_appointments")
    object DoctorHospital       : Screen("doctor_hospital")
    object DoctorNotifications  : Screen("doctor_notifications")
    object NotificationSettings : Screen("notification_settings")
}

fun hospitalDetailRoute(id: Int) = "hospital_detail/$id"
fun doctorProfileRoute(id: Int) = "doctor_profile/$id"
fun bookingRoute(doctorId: Int, slotId: Int) = "booking/$doctorId/$slotId"
fun appointmentConfirmRoute(appointmentId: Int) = "appointment_confirm/$appointmentId"
