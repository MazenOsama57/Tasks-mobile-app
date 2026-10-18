package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HabitFrequency(val label: String) {
    DAILY("Every day"),
    WEEKDAYS("Weekdays (Mon-Fri)"),
    WEEKENDS("Weekends (Sat-Sun)"),
    CUSTOM("Custom days")
}

@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["isArchived"]),
        Index(value = ["orderIndex"])
    ]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDaysPerWeek: Int = 7,
    val colorHex: Long = 0xFF4F46E5, // Default Indigo
    val iconName: String = "fire",
    val customDaysOfWeek: String = "1,2,3,4,5,6,7", // Monday=1 ... Sunday=7
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["dateString"]),
        Index(value = ["habitId", "dateString"], unique = true)
    ]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val completedAt: Long = System.currentTimeMillis()
)

data class HabitWithLogs(
    val habit: Habit,
    val logs: List<HabitLog> = emptyList()
) {
    val completedDateStrings: Set<String> by lazy {
        logs.map { it.dateString }.toSet()
    }

    val currentStreak: Int by lazy {
        calculateCurrentStreak()
    }

    val longestStreak: Int by lazy {
        calculateLongestStreak()
    }

    val isDoneToday: Boolean by lazy {
        isCompletedOn(getTodayDateString())
    }

    val weekCompletionList: List<Pair<String, Boolean>> by lazy {
        calculateWeekCompletion()
    }

    fun isCompletedOn(dateString: String): Boolean {
        return completedDateStrings.contains(dateString)
    }

    fun isCompletedToday(): Boolean {
        return isDoneToday
    }

    fun getWeekCompletion(): List<Pair<String, Boolean>> {
        return weekCompletionList
    }

    /**
     * Calculates the current continuous streak up to today (or yesterday if not yet completed today).
     */
    fun calculateCurrentStreak(): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        var streak = 0
        if (completedDateStrings.contains(todayStr)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check if streak was active as of yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)
            if (!completedDateStrings.contains(yesterdayStr)) {
                return 0
            }
        }

        var iterations = 0
        while (iterations < 10000) {
            val dateStr = dateFormat.format(cal.time)
            if (completedDateStrings.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                iterations++
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Calculates the longest historical streak.
     */
    fun calculateLongestStreak(): Int {
        if (completedDateStrings.isEmpty()) return 0
        val sortedDates = completedDateStrings.sorted()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var maxStreak = 1
        var currentStreak = 1

        for (i in 0 until sortedDates.size - 1) {
            try {
                val d1 = dateFormat.parse(sortedDates[i])
                val d2 = dateFormat.parse(sortedDates[i + 1])
                if (d1 != null && d2 != null) {
                    val diffDays = Math.round((d2.time - d1.time).toDouble() / (1000.0 * 60 * 60 * 24)).toLong()
                    if (diffDays == 1L) {
                        currentStreak++
                        if (currentStreak > maxStreak) {
                            maxStreak = currentStreak
                        }
                    } else if (diffDays > 1L) {
                        currentStreak = 1
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        return maxStreak.coerceAtLeast(calculateCurrentStreak())
    }

    /**
     * Returns an array of 7 booleans representing Monday through Sunday for current week.
     */
    private fun calculateWeekCompletion(): List<Pair<String, Boolean>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)
        val cal = Calendar.getInstance()
        // Find Monday of this week
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val result = mutableListOf<Pair<String, Boolean>>()
        for (i in 0 until 7) {
            val dateStr = dateFormat.format(cal.time)
            val dayLabel = dayLabelFormat.format(cal.time).take(1)
            result.add(Pair(dayLabel, completedDateStrings.contains(dateStr)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    companion object {
        fun getTodayDateString(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return dateFormat.format(Date())
        }

        /**
         * Calculates the master streak for a collection of habits.
         * The top streak only advances if ALL habits on the page are completed for that day.
         * Returns Pair(masterStreakCount, isAllCompletedToday)
         */
        fun calculateAllHabitsStreak(habits: List<HabitWithLogs>): Pair<Int, Boolean> {
            if (habits.isEmpty()) return Pair(0, false)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            val todayStr = dateFormat.format(cal.time)

            val allCompletedToday = habits.all { it.isCompletedOn(todayStr) }
            var streak = 0

            if (allCompletedToday) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(cal.time)
                val allCompletedYesterday = habits.all { it.isCompletedOn(yesterdayStr) }
                if (!allCompletedYesterday) {
                    return Pair(0, false)
                }
            }

            var iterations = 0
            while (iterations < 10000) {
                val dateStr = dateFormat.format(cal.time)
                if (habits.all { it.isCompletedOn(dateStr) }) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    iterations++
                } else {
                    break
                }
            }
            return Pair(streak, allCompletedToday)
        }

        /**
         * Calculates the all-time record streak where ALL habits on the page were completed.
         */
        fun calculateAllHabitsRecordStreak(habits: List<HabitWithLogs>): Int {
            if (habits.isEmpty()) return 0
            val allCompletedSets = habits.map { it.completedDateStrings }
            val commonDates = allCompletedSets.reduce { acc, set -> acc.intersect(set) }
                .toList()
                .sorted()
            if (commonDates.isEmpty()) return 0

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            var maxStreak = 1
            var currentStreak = 1

            for (i in 0 until commonDates.size - 1) {
                try {
                    val d1 = dateFormat.parse(commonDates[i])
                    val d2 = dateFormat.parse(commonDates[i + 1])
                    if (d1 != null && d2 != null) {
                        val diffDays = Math.round((d2.time - d1.time).toDouble() / (1000.0 * 60 * 60 * 24)).toLong()
                        if (diffDays == 1L) {
                            currentStreak++
                            if (currentStreak > maxStreak) {
                                maxStreak = currentStreak
                            }
                        } else if (diffDays > 1L) {
                            currentStreak = 1
                        }
                    }
                } catch (_: Exception) {}
            }
            val (currentMasterStreak, _) = calculateAllHabitsStreak(habits)
            return maxStreak.coerceAtLeast(currentMasterStreak)
        }
    }
}
