package com.example.ui.util

import com.example.data.model.RecurrenceFrequency
import java.text.SimpleDateFormat
import java.util.*

object TaskDateUtils {
    fun formatDueDate(dueDateMillis: Long?, hasTime: Boolean = false): String {
        if (dueDateMillis == null) return ""

        val taskCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val nowCal = Calendar.getInstance()

        val isToday = isSameDay(taskCal, nowCal)

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = isSameDay(taskCal, tomorrowCal)

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = isSameDay(taskCal, yesterdayCal)

        val isSameYear = taskCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)

        val dateStr = when {
            isToday -> "Today"
            isTomorrow -> "Tomorrow"
            isYesterday -> "Yesterday"
            isSameYear -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(taskCal.time)
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(taskCal.time)
        }

        return if (hasTime) {
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(taskCal.time)
            "$dateStr, $timeStr"
        } else {
            dateStr
        }
    }

    fun isOverdue(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return dueDateMillis < todayStart
    }

    fun isDueToday(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val taskCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val nowCal = Calendar.getInstance()
        return isSameDay(taskCal, nowCal)
    }

    fun isDueTomorrow(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val taskCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return isSameDay(taskCal, tomorrowCal)
    }

    fun isFutureDate(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return dueDateMillis > endOfToday
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 18)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTomorrowMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getNextWeekMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Calculates the next due date based on recurrence frequency, interval,
     * and number of times per period.
     */
    fun calculateNextDueDate(
        currentDueDate: Long?,
        frequency: RecurrenceFrequency,
        interval: Int,
        timesPerPeriod: Int,
        daysOfWeek: String = "",
        daysOfMonth: String = ""
    ): Long {
        val baseTime = if (currentDueDate != null && currentDueDate > System.currentTimeMillis()) {
            currentDueDate
        } else {
            System.currentTimeMillis()
        }

        val cal = Calendar.getInstance().apply { timeInMillis = baseTime }
        val safeInterval = if (interval <= 0) 1 else interval
        val safeTimes = if (timesPerPeriod <= 0) 1 else timesPerPeriod

        when (frequency) {
            RecurrenceFrequency.NONE -> return baseTime
            RecurrenceFrequency.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, safeInterval)
            }
            RecurrenceFrequency.WEEKLY -> {
                val selectedDays = daysOfWeek.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..7 }
                if (selectedDays.isNotEmpty()) {
                    // Advance day-by-day until next matching day of week that is in the future
                    for (i in 1..84) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        if (selectedDays.contains(cal.get(Calendar.DAY_OF_WEEK)) && cal.timeInMillis > System.currentTimeMillis()) {
                            break
                        }
                    }
                } else if (safeTimes > 1) {
                    val daysIncrement = (7 * safeInterval) / safeTimes
                    cal.add(Calendar.DAY_OF_YEAR, if (daysIncrement > 0) daysIncrement else 1)
                } else {
                    cal.add(Calendar.WEEK_OF_YEAR, safeInterval)
                }
            }
            RecurrenceFrequency.MONTHLY -> {
                val selectedMonthDays = daysOfMonth.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..31 }
                if (selectedMonthDays.isNotEmpty()) {
                    // Advance day-by-day until next matching day of month that is in the future
                    for (i in 1..366) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                        val isMatch = selectedMonthDays.contains(currentDay) || (selectedMonthDays.any { it > maxDay } && currentDay == maxDay)
                        if (isMatch && cal.timeInMillis > System.currentTimeMillis()) {
                            break
                        }
                    }
                } else if (safeTimes > 1) {
                    val daysIncrement = (30 * safeInterval) / safeTimes
                    cal.add(Calendar.DAY_OF_YEAR, if (daysIncrement > 0) daysIncrement else 1)
                } else {
                    cal.add(Calendar.MONTH, safeInterval)
                }
            }
            RecurrenceFrequency.YEARLY -> {
                if (safeTimes > 1) {
                    val monthsIncrement = (12 * safeInterval) / safeTimes
                    cal.add(Calendar.MONTH, if (monthsIncrement > 0) monthsIncrement else 1)
                } else {
                    cal.add(Calendar.YEAR, safeInterval)
                }
            }
        }
        return cal.timeInMillis
    }

    /**
     * Determines the initial occurrence date for a newly created recurring task.
     * If today matches the recurring pattern, today is chosen; otherwise the earliest
     * upcoming matching date is chosen.
     */
    fun computeInitialOccurrenceDate(
        frequency: RecurrenceFrequency,
        daysOfWeek: String = "",
        daysOfMonth: String = ""
    ): Long {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (frequency) {
            RecurrenceFrequency.NONE -> return nowCal.timeInMillis
            RecurrenceFrequency.DAILY -> return nowCal.timeInMillis
            RecurrenceFrequency.WEEKLY -> {
                val selectedDays = daysOfWeek.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..7 }
                if (selectedDays.isNotEmpty()) {
                    if (selectedDays.contains(nowCal.get(Calendar.DAY_OF_WEEK))) {
                        return nowCal.timeInMillis
                    }
                    val testCal = Calendar.getInstance().apply { timeInMillis = nowCal.timeInMillis }
                    for (i in 1..14) {
                        testCal.add(Calendar.DAY_OF_YEAR, 1)
                        if (selectedDays.contains(testCal.get(Calendar.DAY_OF_WEEK))) {
                            return testCal.timeInMillis
                        }
                    }
                }
                return nowCal.timeInMillis
            }
            RecurrenceFrequency.MONTHLY -> {
                val selectedMonthDays = daysOfMonth.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..31 }
                if (selectedMonthDays.isNotEmpty()) {
                    val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)
                    if (selectedMonthDays.contains(currentDay)) {
                        return nowCal.timeInMillis
                    }
                    val testCal = Calendar.getInstance().apply { timeInMillis = nowCal.timeInMillis }
                    for (i in 1..366) {
                        testCal.add(Calendar.DAY_OF_YEAR, 1)
                        val maxDay = testCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val day = testCal.get(Calendar.DAY_OF_MONTH)
                        if (selectedMonthDays.contains(day) || (selectedMonthDays.any { it > maxDay } && day == maxDay)) {
                            return testCal.timeInMillis
                        }
                    }
                }
                return nowCal.timeInMillis
            }
            RecurrenceFrequency.YEARLY -> return nowCal.timeInMillis
        }
    }
}
