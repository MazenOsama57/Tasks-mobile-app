package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Priority
import com.example.data.model.RecurrenceFrequency

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return try {
            Priority.valueOf(value)
        } catch (e: Exception) {
            Priority.NONE
        }
    }

    @TypeConverter
    fun fromRecurrence(frequency: RecurrenceFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toRecurrence(value: String): RecurrenceFrequency {
        return try {
            RecurrenceFrequency.valueOf(value)
        } catch (e: Exception) {
            RecurrenceFrequency.NONE
        }
    }

    @TypeConverter
    fun fromFocusSessionType(type: com.example.data.model.FocusSessionType): String {
        return type.name
    }

    @TypeConverter
    fun toFocusSessionType(value: String): com.example.data.model.FocusSessionType {
        return try {
            com.example.data.model.FocusSessionType.valueOf(value)
        } catch (e: Exception) {
            com.example.data.model.FocusSessionType.WORK
        }
    }

    @TypeConverter
    fun fromHabitFrequency(frequency: com.example.data.model.HabitFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toHabitFrequency(value: String): com.example.data.model.HabitFrequency {
        return try {
            com.example.data.model.HabitFrequency.valueOf(value)
        } catch (e: Exception) {
            com.example.data.model.HabitFrequency.DAILY
        }
    }
}
