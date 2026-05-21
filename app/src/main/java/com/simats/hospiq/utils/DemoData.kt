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
            id = 1, name = "Apollo Hospital", city = "Chennai",
            type = "Multispecialty", avgRating = 4.8f, totalReviews = 1240,
            address = "21 Greams Lane, Chennai", openingHours = "24 hours",
            photo = null, distance = 1.2f,
            facilities = listOf("ICU", "Emergency", "Pharmacy", "Lab"),
            specialties = listOf("Cardiology", "Neurology", "Orthopedics")
        ),
        Hospital(
            id = 2, name = "MIOT International", city = "Chennai",
            type = "Private", avgRating = 4.6f, totalReviews = 890,
            address = "4/112 Mount Poonamallee Rd", openingHours = "9AM-9PM",
            photo = null, distance = 3.4f,
            facilities = listOf("ICU", "Surgery", "Pharmacy"),
            specialties = listOf("Orthopedics", "Spine", "Sports Medicine")
        ),
        Hospital(
            id = 3, name = "Government General Hospital", city = "Chennai",
            type = "Government", avgRating = 4.1f, totalReviews = 3200,
            address = "Park Town, Chennai", openingHours = "24 hours",
            photo = null, distance = 5.1f,
            facilities = listOf("ICU", "Emergency", "Blood Bank", "Lab"),
            specialties = listOf("General Medicine", "Pediatrics", "OB-GYN")
        ),
        Hospital(
            id = 4, name = "Fortis Malar Hospital", city = "Chennai",
            type = "Private", avgRating = 4.5f, totalReviews = 670,
            address = "52 1st Main Rd, Adyar", openingHours = "24 hours",
            photo = null, distance = 6.8f,
            facilities = listOf("ICU", "Cath Lab", "NICU"),
            specialties = listOf("Cardiology", "Oncology", "Neurology")
        )
    )

    val doctors = listOf(
        Doctor(
            id = 1, name = "Dr. Priya Ramesh", specialization = "Cardiologist",
            hospitalId = 1, hospitalName = "Apollo Hospital",
            rating = 4.9f, totalPatients = 1500, yearsExperience = 12,
            status = "available", photo = null,
            bio = "Senior interventional cardiologist with 12 years experience."
        ),
        Doctor(
            id = 2, name = "Dr. Arjun Mehta", specialization = "Neurologist",
            hospitalId = 1, hospitalName = "Apollo Hospital",
            rating = 4.7f, totalPatients = 980, yearsExperience = 9,
            status = "busy", photo = null,
            bio = "Specialist in stroke management and epilepsy."
        ),
        Doctor(
            id = 3, name = "Dr. Sneha Iyer", specialization = "Orthopedic Surgeon",
            hospitalId = 2, hospitalName = "MIOT International",
            rating = 4.8f, totalPatients = 1200, yearsExperience = 15,
            status = "available", photo = null,
            bio = "Expert in joint replacement and sports injuries."
        ),
        Doctor(
            id = 4, name = "Dr. Kiran Kumar", specialization = "Pediatrician",
            hospitalId = 3, hospitalName = "Govt General Hospital",
            rating = 4.5f, totalPatients = 2100, yearsExperience = 8,
            status = "in_surgery", photo = null,
            bio = "Dedicated pediatrician specializing in neonatal care."
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

    val patientAppointments = listOf(
        Appointment(
            id = 1, doctorName = "Dr. Priya Ramesh",
            specialization = "Cardiologist", hospitalName = "Apollo Hospital",
            date = "2025-06-10", time = "10:00 AM", status = "accepted",
            consultationType = "in_person"
        ),
        Appointment(
            id = 2, doctorName = "Dr. Arjun Mehta",
            specialization = "Neurologist", hospitalName = "Apollo Hospital",
            date = "2025-05-28", time = "02:30 PM", status = "completed",
            consultationType = "video_call"
        ),
        Appointment(
            id = 3, doctorName = "Dr. Sneha Iyer",
            specialization = "Orthopedic Surgeon", hospitalName = "MIOT International",
            date = "2025-05-20", time = "11:00 AM", status = "cancelled",
            consultationType = "in_person"
        ),
        Appointment(
            id = 4, doctorName = "Dr. Kiran Kumar",
            specialization = "Pediatrician", hospitalName = "Govt General Hospital",
            date = "2025-06-15", time = "09:30 AM", status = "pending",
            consultationType = "in_person"
        )
    )

    val doctorAppointments = listOf(
        Appointment(
            id = 1, patientName = "Ravi Shankar", date = "2025-06-05",
            time = "09:00 AM", status = "pending", consultationType = "in_person",
            hospitalName = "Apollo Hospital"
        ),
        Appointment(
            id = 2, patientName = "Meena Devi", date = "2025-06-05",
            time = "10:00 AM", status = "accepted", consultationType = "video_call",
            hospitalName = "Apollo Hospital"
        ),
        Appointment(
            id = 3, patientName = "Suresh Babu", date = "2025-06-05",
            time = "11:00 AM", status = "pending", consultationType = "in_person",
            hospitalName = "Apollo Hospital"
        ),
        Appointment(
            id = 4, patientName = "Anitha R", date = "2025-05-30",
            time = "03:00 PM", status = "completed", consultationType = "in_person",
            hospitalName = "Apollo Hospital"
        )
    )

    val notifications = listOf(
        AppNotification(
            id = 1, title = "Appointment Confirmed",
            body = "Dr. Priya Ramesh has accepted your appointment for June 10 at 10:00 AM",
            type = "appointment", isRead = false, timeAgo = "2 hours ago"
        ),
        AppNotification(
            id = 2, title = "Reminder",
            body = "Your appointment with Dr. Arjun Mehta is tomorrow at 2:30 PM",
            type = "appointment", isRead = false, timeAgo = "5 hours ago"
        ),
        AppNotification(
            id = 3, title = "Rate your visit",
            body = "How was your experience at Apollo Hospital? Share your feedback.",
            type = "rating", isRead = true, timeAgo = "2 days ago"
        ),
        AppNotification(
            id = 4, title = "New Feature",
            body = "You can now book video consultations with doctors directly.",
            type = "general", isRead = true, timeAgo = "3 days ago"
        )
    )

    val specialties = listOf(
        "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
        "Dermatology", "ENT", "Ophthalmology", "General Medicine"
    )

    val currentPatient = User(
        id = 1, name = "Karthik Selvam",
        email = "karthik@email.com", phone = "9876543210", role = "patient"
    )

    val currentDoctor = User(
        id = 2, name = "Dr. Priya Ramesh",
        email = "priya@apollo.com", phone = "9876500001", role = "doctor",
        hospitalId = 1, hospitalName = "Apollo Hospital",
        specialization = "Cardiologist"
    )
}
