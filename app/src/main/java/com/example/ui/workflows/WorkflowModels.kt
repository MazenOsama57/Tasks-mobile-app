package com.example.ui.workflows

import androidx.compose.ui.graphics.Color
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import com.example.ui.util.TaskDateUtils

enum class AppBottomTab(val label: String) {
    TASKS("Tasks"),
    WORKFLOWS("Workflows"),
    HABITS("Habits"),
    ACHIEVEMENTS("Badges")
}

enum class WorkflowViewMode(val label: String) {
    EISENHOWER_MATRIX("Eisenhower Matrix"),
    KANBAN_BOARD("Kanban Board")
}

enum class EisenhowerQuadrant(
    val title: String,
    val subtitle: String,
    val description: String,
    val primaryColor: Color,
    val containerColor: Color
) {
    DO_FIRST(
        title = "Do First",
        subtitle = "Urgent & Important",
        description = "High priority or due soon. Tackle immediately.",
        primaryColor = Color(0xFFEF4444),
        containerColor = Color(0xFFFEE2E2)
    ),
    SCHEDULE(
        title = "Schedule",
        subtitle = "Important, Not Urgent",
        description = "High impact tasks with future deadlines. Plan ahead.",
        primaryColor = Color(0xFF3B82F6),
        containerColor = Color(0xFFDBEAFE)
    ),
    DELEGATE(
        title = "Delegate",
        subtitle = "Urgent, Not Important",
        description = "Immediate tasks with low priority. Automate or finish quickly.",
        primaryColor = Color(0xFFF59E0B),
        containerColor = Color(0xFFFEF3C7)
    ),
    ELIMINATE(
        title = "Eliminate / Backlog",
        subtitle = "Neither Urgent nor Important",
        description = "Low priority ideas. Drop or defer to someday.",
        primaryColor = Color(0xFF6B7280),
        containerColor = Color(0xFFF3F4F6)
    );

    companion object {
        fun classify(task: Task): EisenhowerQuadrant {
            val isUrgent = when {
                task.dueDateMillis == null -> false
                TaskDateUtils.isOverdue(task.dueDateMillis) -> true
                TaskDateUtils.isDueToday(task.dueDateMillis) -> true
                TaskDateUtils.isDueTomorrow(task.dueDateMillis) -> true
                else -> false
            }

            val isImportant = task.priority == Priority.HIGH || task.priority == Priority.MEDIUM || task.isStarred

            return when {
                isUrgent && isImportant -> DO_FIRST
                !isUrgent && isImportant -> SCHEDULE
                isUrgent && !isImportant -> DELEGATE
                else -> ELIMINATE
            }
        }
    }
}

enum class KanbanColumn(
    val title: String,
    val subtitle: String,
    val accentColor: Color
) {
    TODO(
        title = "To Do",
        subtitle = "Backlog & Planned",
        accentColor = Color(0xFF64748B)
    ),
    IN_PROGRESS(
        title = "In Progress",
        subtitle = "Active Focus",
        accentColor = Color(0xFF0284C7)
    ),
    DONE(
        title = "Done",
        subtitle = "Completed",
        accentColor = Color(0xFF10B981)
    );

    companion object {
        fun classify(item: TaskWithSubtasks, activeFocusTaskId: Long? = null): KanbanColumn {
            return when {
                item.task.isCompleted -> DONE
                item.task.id == activeFocusTaskId || item.task.isStarred -> IN_PROGRESS
                else -> TODO
            }
        }
    }
}
