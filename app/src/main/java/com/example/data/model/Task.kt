package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["parentTaskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["parentTaskId"]),
        Index(value = ["isCompleted"]),
        Index(value = ["dueDateMillis"]),
        Index(value = ["isStarred"]),
        Index(value = ["recurrenceFrequency"]),
        Index(value = ["spawnedNextTaskId"])
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long,
    val parentTaskId: Long? = null,
    val title: String,
    val details: String = "",
    val dueDateMillis: Long? = null,
    val hasTime: Boolean = false,
    val priority: Priority = Priority.NONE,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceTimesPerPeriod: Int = 1,
    val recurrenceDaysOfWeek: String = "",
    val recurrenceDaysOfMonth: String = "",
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val orderIndex: Int = 0,
    val spawnedNextTaskId: Long? = null,
    val recurringOriginalDueDate: Long? = null,
    val tags: String = ""
)

fun Task.getTagList(): List<String> {
    if (tags.isBlank()) return emptyList()
    return tags.split(",")
        .map { it.trim().removePrefix("#") }
        .filter { it.isNotEmpty() }
}
