package com.example.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.graphics.toColorInt
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.Priority
import com.example.data.model.TaskWithSubtasks
import com.example.ui.util.TaskDateUtils
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(applicationContext)
    }
}

class TasksRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var tasksList: List<TaskWithSubtasks> = emptyList()

    override fun onCreate() {
        loadTasks()
    }

    override fun onDataSetChanged() {
        loadTasks()
    }

    private fun loadTasks() {
        try {
            val database = AppDatabase.getDatabase(context)
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endOfDay = cal.timeInMillis

            tasksList = runBlocking {
                database.taskDao().getPendingTasksForWidget(endOfDay)
            }
        } catch (e: Exception) {
            tasksList = emptyList()
        }
    }

    override fun onDestroy() {
        tasksList = emptyList()
    }

    override fun getCount(): Int = tasksList.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position !in tasksList.indices) {
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }

        val item = tasksList[position]
        val task = item.task
        val subtasks = item.subtasks

        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        // Task Title
        views.setTextViewText(R.id.tv_widget_item_title, task.title)

        // Priority Badge
        when (task.priority) {
            Priority.HIGH -> {
                views.setViewVisibility(R.id.tv_widget_item_priority, View.VISIBLE)
                views.setTextViewText(R.id.tv_widget_item_priority, "HIGH")
                views.setInt(R.id.tv_widget_item_priority, "setBackgroundResource", R.drawable.widget_badge_high)
                views.setTextColor(R.id.tv_widget_item_priority, "#FB7185".toColorInt())
            }
            Priority.MEDIUM -> {
                views.setViewVisibility(R.id.tv_widget_item_priority, View.VISIBLE)
                views.setTextViewText(R.id.tv_widget_item_priority, "MED")
                views.setInt(R.id.tv_widget_item_priority, "setBackgroundResource", R.drawable.widget_badge_med)
                views.setTextColor(R.id.tv_widget_item_priority, "#FBBF24".toColorInt())
            }
            Priority.LOW -> {
                views.setViewVisibility(R.id.tv_widget_item_priority, View.VISIBLE)
                views.setTextViewText(R.id.tv_widget_item_priority, "LOW")
                views.setInt(R.id.tv_widget_item_priority, "setBackgroundResource", R.drawable.widget_badge_low)
                views.setTextColor(R.id.tv_widget_item_priority, "#34D399".toColorInt())
            }
            Priority.NONE -> {
                views.setViewVisibility(R.id.tv_widget_item_priority, View.GONE)
            }
        }

        // Due Date Tag
        if (task.dueDateMillis != null) {
            val dueStr = TaskDateUtils.formatDueDate(task.dueDateMillis, task.hasTime)
            val isOverdue = TaskDateUtils.isOverdue(task.dueDateMillis)
            views.setViewVisibility(R.id.tv_widget_item_due, View.VISIBLE)
            views.setTextViewText(R.id.tv_widget_item_due, if (isOverdue) "Overdue ($dueStr)" else dueStr)
            views.setTextColor(
                R.id.tv_widget_item_due,
                if (isOverdue) "#FB7185".toColorInt() else "#8AB4F8".toColorInt()
            )
        } else {
            views.setViewVisibility(R.id.tv_widget_item_due, View.GONE)
        }

        // Subtasks Tag
        if (subtasks.isNotEmpty()) {
            val completedSubs = subtasks.count { it.isCompleted }
            views.setViewVisibility(R.id.tv_widget_item_subtasks, View.VISIBLE)
            views.setTextViewText(R.id.tv_widget_item_subtasks, "$completedSubs/${subtasks.size} subtasks")
        } else {
            views.setViewVisibility(R.id.tv_widget_item_subtasks, View.GONE)
        }

        // Fill-in Intent for Quick Completion directly from widget
        val fillInIntent = Intent().apply {
            putExtra(TasksWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.btn_widget_check, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_task_row, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in tasksList.indices) tasksList[position].task.id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
