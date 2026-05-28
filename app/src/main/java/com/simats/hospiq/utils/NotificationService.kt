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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.simats.hospiq.network.RetrofitInstance

object NotificationService {
    private const val CHANNEL_APPOINTMENT = "hospiq_appointments"
    private const val CHANNEL_REMINDER = "hospiq_reminders"
    private const val CHANNEL_GENERAL = "hospiq_general"

    // Thread-safe process-level cache to prevent concurrent double-triggering
    private val notifiedIds = java.util.Collections.synchronizedSet(HashSet<Int>())
    @Volatile private var isChecking = false

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

    fun isNotified(context: Context, userId: Int, id: Int): Boolean {
        if (notifiedIds.contains(id)) return true
        
        val prefs = context.getSharedPreferences("hospiq_notif_prefs", Context.MODE_PRIVATE)
        val shownIdsStr = prefs.getString("shown_ids_list_$userId", "") ?: ""
        val idList = shownIdsStr.split(",")
        if (idList.contains(id.toString())) {
            notifiedIds.add(id)
            return true
        }
        return false
    }

    fun markAsNotified(context: Context, userId: Int, id: Int) {
        notifiedIds.add(id)
        
        val prefs = context.getSharedPreferences("hospiq_notif_prefs", Context.MODE_PRIVATE)
        val shownIdsStr = prefs.getString("shown_ids_list_$userId", "") ?: ""
        val updatedStr = if (shownIdsStr.isEmpty()) id.toString() else "$shownIdsStr,${id}"
        prefs.edit().putString("shown_ids_list_$userId", updatedStr).apply()
    }

    fun clearCache() {
        notifiedIds.clear()
    }

    fun checkForNewNotifications(context: Context, userId: Int) {
        synchronized(this) {
            if (isChecking) return
            isChecking = true
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getNotifications(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notifications = response.body()!!.data?.notifications ?: return@launch
                    
                    val mainPrefs = context.getSharedPreferences("hospiq_prefs", Context.MODE_PRIVATE)
                    val firstFetchKey = "first_fetch_done_$userId"
                    val isFirstFetch = !mainPrefs.getBoolean(firstFetchKey, false)
                    
                    if (isFirstFetch) {
                        // Mark all existing unread notifications as notified on first fetch (login/fresh boot)
                        for (notif in notifications) {
                            markAsNotified(context, userId, notif.id)
                        }
                        mainPrefs.edit().putBoolean(firstFetchKey, true).apply()
                    } else {
                        // Normal real-time check for newly generated unread alerts
                        for (notif in notifications) {
                            if (!notif.isRead && !isNotified(context, userId, notif.id)) {
                                showNotification(
                                    context = context,
                                    channelId = when (notif.type) {
                                        "appointment" -> CHANNEL_APPOINTMENT
                                        "reminder" -> CHANNEL_REMINDER
                                        else -> CHANNEL_GENERAL
                                    },
                                    title = notif.title,
                                    body = notif.body,
                                    notifId = notif.id
                                )
                                markAsNotified(context, userId, notif.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                synchronized(this@NotificationService) {
                    isChecking = false
                }
            }
        }
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
