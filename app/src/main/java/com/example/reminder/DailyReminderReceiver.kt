package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val settings = database.settingsDao().getSettingsOnce()

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endOfToday = cal.timeInMillis

                when (action) {
                    DailyReminderScheduler.ACTION_MORNING -> {
                        val pendingCount = database.taskDao().getPendingTodayCount(endOfToday)
                        val title = "Good morning! ☀️"
                        val content = if (pendingCount > 0) {
                            "You have $pendingCount task(s) on your agenda for today. Tap to view your plan!"
                        } else {
                            "Your day is clear! Tap to review goals or add new tasks."
                        }
                        DailyReminderScheduler.showNotification(
                            context = context,
                            notificationId = DailyReminderScheduler.NOTIF_ID_MORNING,
                            title = title,
                            content = content
                        )

                        // Reschedule next morning
                        if (settings?.morningReminderEnabled == true) {
                            DailyReminderScheduler.scheduleReminders(
                                context = context,
                                morningEnabled = true,
                                morningHour = settings.morningReminderHour,
                                morningMinute = settings.morningReminderMinute,
                                eveningEnabled = settings.eveningReminderEnabled,
                                eveningHour = settings.eveningReminderHour,
                                eveningMinute = settings.eveningReminderMinute
                            )
                        }
                    }

                    DailyReminderScheduler.ACTION_EVENING -> {
                        val pendingCount = database.taskDao().getPendingTodayCount(endOfToday)
                        if (pendingCount > 0) {
                            DailyReminderScheduler.showNotification(
                                context = context,
                                notificationId = DailyReminderScheduler.NOTIF_ID_EVENING,
                                title = "Evening Wrap-up 🌙",
                                content = "You have $pendingCount uncompleted task(s) remaining for today. Finish strong before the day ends!"
                            )
                        }

                        // Reschedule next evening
                        if (settings?.eveningReminderEnabled == true) {
                            DailyReminderScheduler.scheduleReminders(
                                context = context,
                                morningEnabled = settings.morningReminderEnabled,
                                morningHour = settings.morningReminderHour,
                                morningMinute = settings.morningReminderMinute,
                                eveningEnabled = true,
                                eveningHour = settings.eveningReminderHour,
                                eveningMinute = settings.eveningReminderMinute
                            )
                        }
                    }

                    DailyReminderScheduler.ACTION_TEST -> {
                        DailyReminderScheduler.triggerTestNotification(context)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
