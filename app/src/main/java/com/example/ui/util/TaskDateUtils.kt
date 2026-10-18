package com.example.ui.util

import com.example.data.model.RecurrenceFrequency
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TaskDateUtils {

    @Volatile
    private var lastCacheCheckMillis: Long = 0L
    @Volatile
    private var cachedStartOfToday: Long = 0L
    @Volatile
    private var cachedEndOfToday: Long = 0L
    @Volatile
    private var cachedStartOfTomorrow: Long = 0L
    @Volatile
    private var cachedEndOfTomorrow: Long = 0L
    @Volatile
    private var cachedStartOfYesterday: Long = 0L
    @Volatile
    private var cachedEndOfYesterday: Long = 0L
    @Volatile
    private var cachedCurrentYear: Int = 0

    // High-performance caches to prevent object churn during scrolling and page switching
    private val formattedDateCache = ConcurrentHashMap<Long, String>()
    private val formattedDateTimeCache = ConcurrentHashMap<Long, String>()
    private val initialOccurrenceCache = ConcurrentHashMap<String, Long>()

    private fun ensureTimeBoundaries() {
        val now = System.currentTimeMillis()
        if (now < cachedStartOfToday || now > cachedEndOfToday || now - lastCacheCheckMillis > 30_000L) {
            synchronized(this) {
                if (now < cachedStartOfToday || now > cachedEndOfToday || now - lastCacheCheckMillis > 30_000L) {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = now
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayStart = cal.timeInMillis
                    val dayMs = 86_400_000L
                    cachedStartOfToday = todayStart
                    cachedEndOfToday = todayStart + dayMs - 1L
                    cachedStartOfTomorrow = todayStart + dayMs
                    cachedEndOfTomorrow = todayStart + 2 * dayMs - 1L
                    cachedStartOfYesterday = todayStart - dayMs
                    cachedEndOfYesterday = todayStart - 1L
                    cachedCurrentYear = cal.get(Calendar.YEAR)
                    lastCacheCheckMillis = now
                    formattedDateCache.clear()
                    formattedDateTimeCache.clear()
                    initialOccurrenceCache.clear()
                }
            }
        }
    }

    fun getStartOfTodayMillis(): Long {
        ensureTimeBoundaries()
        return cachedStartOfToday
    }

    fun getEndOfTodayMillis(): Long {
        ensureTimeBoundaries()
        return cachedEndOfToday
    }

    fun toStartOfDayMillis(millis: Long): Long {
        ensureTimeBoundaries()
        // Fast path for today, tomorrow, yesterday
        if (millis in cachedStartOfToday..cachedEndOfToday) return cachedStartOfToday
        if (millis in cachedStartOfTomorrow..cachedEndOfTomorrow) return cachedStartOfTomorrow
        if (millis in cachedStartOfYesterday..cachedEndOfYesterday) return cachedStartOfYesterday

        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatDueDate(dueDateMillis: Long?, hasTime: Boolean = false): String {
        if (dueDateMillis == null) return ""
        ensureTimeBoundaries()

        val cache = if (hasTime) formattedDateTimeCache else formattedDateCache
        val cached = cache[dueDateMillis]
        if (cached != null) return cached

        val taskCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }

        val dateStr = when {
            dueDateMillis in cachedStartOfToday..cachedEndOfToday -> "Today"
            dueDateMillis in cachedStartOfTomorrow..cachedEndOfTomorrow -> "Tomorrow"
            dueDateMillis in cachedStartOfYesterday..cachedEndOfYesterday -> "Yesterday"
            taskCal.get(Calendar.YEAR) == cachedCurrentYear -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(taskCal.time)
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(taskCal.time)
        }

        val result = if (hasTime) {
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(taskCal.time)
            "$dateStr, $timeStr"
        } else {
            dateStr
        }

        cache[dueDateMillis] = result
        return result
    }

    fun isOverdue(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        ensureTimeBoundaries()
        return dueDateMillis < cachedStartOfToday
    }

    fun isDueToday(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        ensureTimeBoundaries()
        return dueDateMillis in cachedStartOfToday..cachedEndOfToday
    }

    fun isDueTomorrow(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        ensureTimeBoundaries()
        return dueDateMillis in cachedStartOfTomorrow..cachedEndOfTomorrow
    }

    fun isFutureDate(dueDateMillis: Long?): Boolean {
        if (dueDateMillis == null) return false
        ensureTimeBoundaries()
        return dueDateMillis > cachedEndOfToday
    }

    fun getTodayMillis(): Long {
        ensureTimeBoundaries()
        return cachedStartOfToday + (18 * 3600 * 1000L) // 18:00 today
    }

    fun getTomorrowMillis(): Long {
        ensureTimeBoundaries()
        return cachedStartOfTomorrow + (9 * 3600 * 1000L) // 09:00 tomorrow
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
     * upcoming matching date is chosen. Caches results to prevent repeated 366-day loops.
     */
    fun computeInitialOccurrenceDate(
        frequency: RecurrenceFrequency,
        daysOfWeek: String = "",
        daysOfMonth: String = ""
    ): Long {
        ensureTimeBoundaries()
        val cacheKey = "${frequency.name}_${daysOfWeek}_${daysOfMonth}_$cachedStartOfToday"
        initialOccurrenceCache[cacheKey]?.let { return it }

        val nowCal = Calendar.getInstance().apply {
            timeInMillis = cachedStartOfToday + (18 * 3600 * 1000L)
        }
        val computed = when (frequency) {
            RecurrenceFrequency.NONE -> nowCal.timeInMillis
            RecurrenceFrequency.DAILY -> nowCal.timeInMillis
            RecurrenceFrequency.WEEKLY -> {
                val selectedDays = daysOfWeek.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..7 }
                if (selectedDays.isNotEmpty()) {
                    if (selectedDays.contains(nowCal.get(Calendar.DAY_OF_WEEK))) {
                        nowCal.timeInMillis
                    } else {
                        val testCal = Calendar.getInstance().apply { timeInMillis = nowCal.timeInMillis }
                        var foundTime = nowCal.timeInMillis
                        for (i in 1..14) {
                            testCal.add(Calendar.DAY_OF_YEAR, 1)
                            if (selectedDays.contains(testCal.get(Calendar.DAY_OF_WEEK))) {
                                foundTime = testCal.timeInMillis
                                break
                            }
                        }
                        foundTime
                    }
                } else {
                    nowCal.timeInMillis
                }
            }
            RecurrenceFrequency.MONTHLY -> {
                val selectedMonthDays = daysOfMonth.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .filter { it in 1..31 }
                if (selectedMonthDays.isNotEmpty()) {
                    val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)
                    if (selectedMonthDays.contains(currentDay)) {
                        nowCal.timeInMillis
                    } else {
                        val testCal = Calendar.getInstance().apply { timeInMillis = nowCal.timeInMillis }
                        var foundTime = nowCal.timeInMillis
                        for (i in 1..366) {
                            testCal.add(Calendar.DAY_OF_YEAR, 1)
                            val maxDay = testCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            val day = testCal.get(Calendar.DAY_OF_MONTH)
                            if (selectedMonthDays.contains(day) || (selectedMonthDays.any { it > maxDay } && day == maxDay)) {
                                foundTime = testCal.timeInMillis
                                break
                            }
                        }
                        foundTime
                    }
                } else {
                    nowCal.timeInMillis
                }
            }
            RecurrenceFrequency.YEARLY -> nowCal.timeInMillis
        }

        initialOccurrenceCache[cacheKey] = computed
        return computed
    }
}
