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
        subtitle = "High Priority (Urgent & Important)",
        description = "High-priority tasks. Tackle immediately.",
        primaryColor = Color(0xFFEF4444),
        containerColor = Color(0xFFFEE2E2)
    ),
    SCHEDULE(
        title = "Schedule",
        subtitle = "Medium Priority (Important, Plan Ahead)",
        description = "Medium-priority tasks. Plan ahead and execute with focus.",
        primaryColor = Color(0xFF3B82F6),
        containerColor = Color(0xFFDBEAFE)
    ),
    DELEGATE(
        title = "Delegate",
        subtitle = "Low Priority (Quick / Hand Off)",
        description = "Low-priority tasks. Knock out quickly, automate, or delegate.",
        primaryColor = Color(0xFFF59E0B),
        containerColor = Color(0xFFFEF3C7)
    ),
    ELIMINATE(
        title = "Backlog & Notes",
        subtitle = "Non-Important (Self-Added / Backlog)",
        description = "Non-important tasks, scratchpad ideas, and backlog elements you choose to add.",
        primaryColor = Color(0xFF8B5CF6),
        containerColor = Color(0xFFEDE9FE)
    );

    companion object {
        fun classify(task: Task): EisenhowerQuadrant {
            return when (task.priority) {
                Priority.HIGH -> DO_FIRST
                Priority.MEDIUM -> SCHEDULE
                Priority.LOW -> DELEGATE
                Priority.NONE -> ELIMINATE
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
