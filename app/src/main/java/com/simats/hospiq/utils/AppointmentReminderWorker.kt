package com.simats.hospiq.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.simats.hospiq.network.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AppointmentReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Get userId from session
        val session = SessionManager(appContext)
        val userId = session.getUserId()
        if (userId == -1) return Result.success()

        // 2. Read reminder preferences
        val prefs = appContext.getSharedPreferences("hospiq_prefs", Context.MODE_PRIVATE)
        val reminderMinutes = prefs.getInt("notif_reminder_minutes", 30)
        val isNotificationsEnabled = prefs.getBoolean("notif_enabled", true)

        // 3. Check if notifications are enabled
        if (!isNotificationsEnabled) return Result.success()

        // 4. Fetch appointments from network
        try {
            val response = RetrofitInstance.api.getPatientAppointments(userId)
            if (!response.isSuccessful || response.body()?.success != true) return Result.success()

            val appointments = response.body()?.data?.appointments ?: return Result.success()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val now = System.currentTimeMillis()

            // 5. Iterate through accepted appointments
            for (appointment in appointments) {
                if (appointment.status.lowercase(Locale.getDefault()) != "accepted") continue

                try {
                    // 6. Parse appointment date and time into a combined datetime
                    val datePart = appointment.date   // e.g. "2026-05-27"
                    val timePart = appointment.time   // e.g. "09:30:00"
                    val combinedStr = "$datePart $timePart"
                    val appointmentDateTime: Date = dateTimeFormat.parse(combinedStr) ?: continue

                    // 7. Compute minutes until appointment
                    val minutesUntil = (appointmentDateTime.time - now) / 60_000L

                    // 8. Fire notification if within ±5-minute window of the reminder lead time
                    if (minutesUntil in (reminderMinutes - 5)..(reminderMinutes + 5)) {
                        NotificationService.showReminderNotification(
                            context = appContext,
                            title = "⏰ Upcoming Appointment",
                            body = "You have an appointment with Dr. ${appointment.doctorName} at ${appointment.time} today. Don't be late!",
                            notifId = appointment.id
                        )
                    }
                } catch (parseEx: Exception) {
                    parseEx.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Non-fatal – return success so WorkManager doesn't back-off aggressively
        }

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "appointment_reminder"

        /**
         * Enqueues a periodic reminder check every 15 minutes (minimum WorkManager interval).
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so an already-scheduled worker is not restarted.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AppointmentReminderWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
