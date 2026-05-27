package com.simats.hospiq.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.simats.hospiq.MainActivity
import com.simats.hospiq.R

object NotificationService {
    private const val CHANNEL_APPOINTMENT = "hospiq_appointments"
    private const val CHANNEL_REMINDER = "hospiq_reminders"
    private const val CHANNEL_GENERAL = "hospiq_general"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_APPOINTMENT, "Appointments", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "New appointments and status changes"
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_REMINDER, "Appointment Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Reminders before your appointment"
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General notifications from HospiQ"
                }
            )
        }
    }

    fun showAppointmentNotification(
        context: Context,
        title: String,
        body: String,
        notifId: Int = System.currentTimeMillis().toInt()
    ) {
        showNotification(context, CHANNEL_APPOINTMENT, title, body, notifId)
    }

    fun showReminderNotification(
        context: Context,
        title: String,
        body: String,
        notifId: Int = System.currentTimeMillis().toInt()
    ) {
        showNotification(context, CHANNEL_REMINDER, title, body, notifId)
    }

    fun showGeneralNotification(
        context: Context,
        title: String,
        body: String,
        notifId: Int = System.currentTimeMillis().toInt()
    ) {
        showNotification(context, CHANNEL_GENERAL, title, body, notifId)
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        notifId: Int
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
