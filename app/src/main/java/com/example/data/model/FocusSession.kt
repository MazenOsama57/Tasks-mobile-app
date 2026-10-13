package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FocusSessionType(val label: String, val defaultMinutes: Int) {
    WORK("Focus Work", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["completedAt"])
    ]
)
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String = "",
    val durationMinutes: Int = 25,
    val sessionType: FocusSessionType = FocusSessionType.WORK,
    val completedAt: Long = System.currentTimeMillis()
)
