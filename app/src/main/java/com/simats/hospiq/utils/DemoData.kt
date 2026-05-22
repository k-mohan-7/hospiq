package com.simats.hospiq.utils

import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.network.models.AppNotification
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.Hospital
import com.simats.hospiq.network.models.TimeSlot
import com.simats.hospiq.network.models.User

object DemoData {

    val hospitals = listOf(
        Hospital(
            id = 5, name = "mohan hospitals", city = "Chennai",
            type = "Private", avgRating = 4.8f, totalReviews = 10,
            address = "poonamalle chennai", openingHours = "9:00 AM - 8:00 PM",
            photo = "6a0f412847ac6_hospital_1779384587623.png", distance = 1.2f,
            facilities = listOf("ICU", "Emergency", "Pharmacy", "Lab"),
            specialties = listOf("heart")
        )
    )

    var doctors = listOf(
        Doctor(
            id = 4, userId = 6, name = "Dr. Suresh", specialization = "heart",
            hospitalId = 5, hospitalName = "mohan hospitals", rating = 4.9f,
            totalPatients = 120, yearsExperience = 5, status = "available",
            photo = "uploads/doctors/6a0f41284cd53_doctor_1779384512859.png", bio = "heart specialist"
        )
    )

    val timeSlots = listOf(
        TimeSlot(id = 1, slotTime = "09:00 AM", isBooked = false),
        TimeSlot(id = 2, slotTime = "09:30 AM", isBooked = true),
        TimeSlot(id = 3, slotTime = "10:00 AM", isBooked = false),
        TimeSlot(id = 4, slotTime = "10:30 AM", isBooked = false),
        TimeSlot(id = 5, slotTime = "11:00 AM", isBooked = true),
        TimeSlot(id = 6, slotTime = "11:30 AM", isBooked = false),
        TimeSlot(id = 7, slotTime = "02:00 PM", isBooked = false),
        TimeSlot(id = 8, slotTime = "02:30 PM", isBooked = false),
        TimeSlot(id = 9, slotTime = "03:00 PM", isBooked = true),
        TimeSlot(id = 10, slotTime = "03:30 PM", isBooked = false),
        TimeSlot(id = 11, slotTime = "04:00 PM", isBooked = false),
        TimeSlot(id = 12, slotTime = "04:30 PM", isBooked = false)
    )

    var activeSlots = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    var dynamicTimingsEnabled = true
    var selectedActiveDates = mutableListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val patientAppointments = mutableListOf(
        Appointment(
            id = 101, patientId = 1, doctorId = 4, hospitalId = 5,
            doctorName = "Dr. Suresh", patientName = "Karthik Selvam",
            specialization = "heart", hospitalName = "mohan hospitals",
            date = "2026-05-24", time = "09:30 AM", status = "accepted",
            consultationType = "in_person",
            illnessName = "Chest Discomfort",
            illnessDescription = "Experiencing slight pressure in chest when walking quickly",
            precautions = "Resting, avoid high exertion",
            doctorAdvice = "Keep blood pressure checked daily. Take aspirin if severe and visit immediate ER.",
            doctorStatus = "busy"
        ),
        Appointment(
            id = 102, patientId = 1, doctorId = 4, hospitalId = 5,
            doctorName = "Dr. Suresh", patientName = "Karthik Selvam",
            specialization = "heart", hospitalName = "mohan hospitals",
            date = "2026-05-20", time = "03:00 PM", status = "completed",
            consultationType = "in_person",
            illnessName = "Regular Checkup",
            illnessDescription = "Routine post-surgery recovery checkup",
            precautions = "Taking prescribed beta blockers",
            doctorAdvice = "Heart recovery is looking great! Continue current low-sodium diet and daily walking.",
            doctorStatus = "available"
        )
    )

    val doctorAppointments = mutableListOf(
        Appointment(
            id = 101, patientId = 1, doctorId = 4, hospitalId = 5,
            doctorName = "Dr. Suresh", patientName = "Karthik Selvam",
            specialization = "heart", hospitalName = "mohan hospitals",
            date = "2026-05-24", time = "09:30 AM", status = "accepted",
            consultationType = "in_person",
            illnessName = "Chest Discomfort",
            illnessDescription = "Experiencing slight pressure in chest when walking quickly",
            precautions = "Resting, avoid high exertion",
            doctorAdvice = "Keep blood pressure checked daily. Take aspirin if severe and visit immediate ER.",
            doctorStatus = "busy"
        ),
        Appointment(
            id = 102, patientId = 1, doctorId = 4, hospitalId = 5,
            doctorName = "Dr. Suresh", patientName = "Karthik Selvam",
            specialization = "heart", hospitalName = "mohan hospitals",
            date = "2026-05-20", time = "03:00 PM", status = "completed",
            consultationType = "in_person",
            illnessName = "Regular Checkup",
            illnessDescription = "Routine post-surgery recovery checkup",
            precautions = "Taking prescribed beta blockers",
            doctorAdvice = "Heart recovery is looking great! Continue current low-sodium diet and daily walking.",
            doctorStatus = "available"
        )
    )

    val notifications = mutableListOf<AppNotification>()

    val specialties = listOf(
        "heart", "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
        "Dermatology", "ENT", "Ophthalmology", "General Medicine"
    )

    val currentPatient = User(
        id = 1, name = "Karthik Selvam",
        email = "karthik@email.com", phone = "9876543210", role = "patient"
    )

    val currentDoctor = User(
        id = 2, name = "Dr. Suresh",
        email = "suresh@email.com", phone = "9876500001", role = "doctor",
        hospitalId = 5, hospitalName = "mohan hospitals",
        specialization = "heart"
    )
}
