package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val settings = database.settingsDao().getSettingsOnce()
                    if (settings != null) {
                        DailyReminderScheduler.scheduleReminders(
                            context = context,
                            morningEnabled = settings.morningReminderEnabled,
                            morningHour = settings.morningReminderHour,
                            morningMinute = settings.morningReminderMinute,
                            eveningEnabled = settings.eveningReminderEnabled,
                            eveningHour = settings.eveningReminderHour,
                            eveningMinute = settings.eveningReminderMinute
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
