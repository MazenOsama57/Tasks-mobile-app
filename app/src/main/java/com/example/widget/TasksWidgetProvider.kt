package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.widget.ACTION_COMPLETE_TASK"
        const val ACTION_REFRESH = "com.example.widget.ACTION_REFRESH"
        const val EXTRA_TASK_ID = "extra_task_id"

        @Suppress("DEPRECATION")
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TasksWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_tasks_list)
                val provider = TasksWidgetProvider()
                provider.onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // First synchronously provide initial views so launcher never sees an uninitialized state
        for (widgetId in appWidgetIds) {
            val initialViews = buildRemoteViews(context, widgetId, "Today's Tasks", 0)
            appWidgetManager.updateAppWidget(widgetId, initialViews)
        }

        // Query database asynchronously and update with live counts and progress
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val taskDao = database.taskDao()

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val endOfDay = cal.timeInMillis

                val completedCount = taskDao.getCompletedTodayCount(startOfDay)
                val pendingCount = taskDao.getPendingTodayCount(endOfDay)
                val totalToday = completedCount + pendingCount

                val progressPercent = if (totalToday > 0) {
                    ((completedCount.toFloat() / totalToday) * 100).toInt().coerceIn(0, 100)
                } else {
                    100
                }

                val summaryText = if (totalToday == 0) {
                    "No pending tasks for today"
                } else if (pendingCount == 0) {
                    "All $totalToday done today! (100%)"
                } else {
                    "$completedCount of $totalToday completed ($progressPercent%)"
                }

                for (widgetId in appWidgetIds) {
                    val views = buildRemoteViews(context, widgetId, summaryText, progressPercent)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } catch (e: Exception) {
                // Keep initial views if error
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun buildRemoteViews(
        context: Context,
        widgetId: Int,
        summaryText: String,
        progressPercent: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_tasks_layout)

        views.setTextViewText(R.id.tv_widget_summary, summaryText)
        views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)

        // RemoteAdapter for ListView
        val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            data = toUri(Intent.URI_INTENT_SCHEME).toUri()
        }
        views.setRemoteAdapter(R.id.widget_tasks_list, serviceIntent)
        views.setEmptyView(R.id.widget_tasks_list, R.id.widget_empty_view)

        // Launch app on header / empty view click
        val appOpenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appOpenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appOpenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tv_widget_header_title, appOpenPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_empty_view, appOpenPendingIntent)
        views.setOnClickPendingIntent(R.id.btn_widget_add_task, appOpenPendingIntent)

        // Refresh action
        val refreshIntent = Intent(context, TasksWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            setPackage(context.packageName)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

        // Item click PendingIntent template for ListView
        val itemClickIntent = Intent(context, TasksWidgetProvider::class.java).apply {
            action = ACTION_COMPLETE_TASK
            setPackage(context.packageName)
        }
        val itemClickPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            itemClickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_tasks_list, itemClickPendingIntent)

        return views
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = AppDatabase.getDatabase(context.applicationContext)
                            val repository = TaskRepository(
                                database.taskListDao(),
                                database.taskDao(),
                                database.settingsDao(),
                                context.applicationContext
                            )
                            repository.setTaskCompleted(taskId, true)
                        } catch (e: Exception) {
                            // Handled
                        }
                    }
                }
            }
            ACTION_REFRESH -> {
                updateAllWidgets(context)
            }
        }
    }
}
