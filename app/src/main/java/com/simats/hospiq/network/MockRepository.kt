package com.simats.hospiq.network

import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.Hospital
import com.simats.hospiq.utils.DemoData

class MockRepository : Repository {
    override suspend fun getAllHospitals(): List<Hospital> = DemoData.hospitals
    override suspend fun getNearbyHospitals(lat: Double, lng: Double): List<Hospital> = DemoData.hospitals
    override suspend fun getHospitalById(id: Int): Hospital? = DemoData.hospitals.find { it.id == id }
    override suspend fun getDoctorsByHospital(hospitalId: Int): List<Doctor> = DemoData.doctors.filter { it.hospitalId == hospitalId }
    override suspend fun getDoctorById(doctorId: Int): Doctor? = DemoData.doctors.find { it.id == doctorId }
    override suspend fun getPatientAppointments(patientId: Int): List<Appointment> = DemoData.patientAppointments
    override suspend fun getDoctorAppointments(doctorId: Int): List<Appointment> = DemoData.doctorAppointments
    override suspend fun bookAppointment(patientId: Int, doctorId: Int, slotId: Int, date: String, consultationType: String): Int = 101
    override suspend fun acceptAppointment(appointmentId: Int, doctorId: Int): Boolean = true
    override suspend fun rejectAppointment(appointmentId: Int, doctorId: Int): Boolean = true
    override suspend fun rescheduleAppointment(appointmentId: Int, doctorId: Int, newSlotId: Int, newDate: String, reason: String): Boolean = true
    override suspend fun updateDoctorStatus(doctorId: Int, status: String): Boolean = true
}
