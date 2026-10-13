package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object DailyReminderScheduler {
    const val CHANNEL_ID = "daily_task_reminders"
    const val ACTION_MORNING = "com.example.reminder.ACTION_MORNING"
    const val ACTION_EVENING = "com.example.reminder.ACTION_EVENING"
    const val ACTION_TEST = "com.example.reminder.ACTION_TEST"

    const val NOTIF_ID_MORNING = 1001
    const val NOTIF_ID_EVENING = 1002
    const val NOTIF_ID_TEST = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Task Reminders"
            val descriptionText = "Morning day planning and evening task wrap-up notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled if notification permission is not granted
        }
    }

    fun scheduleReminders(
        context: Context,
        morningEnabled: Boolean,
        morningHour: Int,
        morningMinute: Int,
        eveningEnabled: Boolean,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Morning alarm
        val morningIntent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_MORNING
            setPackage(context.packageName)
        }
        val morningPending = PendingIntent.getBroadcast(
            context,
            1,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (morningEnabled) {
            val morningCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, morningHour)
                set(Calendar.MINUTE, morningMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            scheduleAlarm(alarmManager, morningCal.timeInMillis, morningPending)
        } else {
            alarmManager.cancel(morningPending)
        }

        // Evening alarm
        val eveningIntent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_EVENING
            setPackage(context.packageName)
        }
        val eveningPending = PendingIntent.getBroadcast(
            context,
            2,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (eveningEnabled) {
            val eveningCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, eveningHour)
                set(Calendar.MINUTE, eveningMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            scheduleAlarm(alarmManager, eveningCal.timeInMillis, eveningPending)
        } else {
            alarmManager.cancel(eveningPending)
        }
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: Exception) {}
    }

    fun triggerTestNotification(context: Context) {
        showNotification(
            context = context,
            notificationId = NOTIF_ID_TEST,
            title = "Task Reminder Active 🔔",
            content = "Notifications are working properly! You will receive reminders at your scheduled times."
        )
    }
}
