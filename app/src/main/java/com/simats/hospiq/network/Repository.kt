package com.simats.hospiq.network

import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.Hospital

interface Repository {
    suspend fun getAllHospitals(): List<Hospital>
    suspend fun getNearbyHospitals(lat: Double, lng: Double): List<Hospital>
    suspend fun getHospitalById(id: Int): Hospital?
    suspend fun getDoctorsByHospital(hospitalId: Int): List<Doctor>
    suspend fun getDoctorById(doctorId: Int): Doctor?
    suspend fun getPatientAppointments(patientId: Int): List<Appointment>
    suspend fun getDoctorAppointments(doctorId: Int): List<Appointment>
    suspend fun bookAppointment(patientId: Int, doctorId: Int, slotId: Int, date: String, consultationType: String): Int
    suspend fun acceptAppointment(appointmentId: Int, doctorId: Int): Boolean
    suspend fun rejectAppointment(appointmentId: Int, doctorId: Int): Boolean
    suspend fun rescheduleAppointment(appointmentId: Int, doctorId: Int, newSlotId: Int, newDate: String, reason: String): Boolean
    suspend fun updateDoctorStatus(doctorId: Int, status: String): Boolean
}
