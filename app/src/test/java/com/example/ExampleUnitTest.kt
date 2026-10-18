package com.example

import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitWithLogs
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun individualHabit_hasIndependentStreakAndRecord() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgoStr = dateFormat.format(cal.time)

        val habit1 = Habit(id = 1, title = "Morning Run")
        val habit1WithLogs = HabitWithLogs(
            habit = habit1,
            logs = listOf(
                HabitLog(habitId = 1, dateString = todayStr),
                HabitLog(habitId = 1, dateString = yesterdayStr),
                HabitLog(habitId = 1, dateString = twoDaysAgoStr)
            )
        )

        val habit2 = Habit(id = 2, title = "Read 20 Pages")
        val habit2WithLogs = HabitWithLogs(
            habit = habit2,
            logs = listOf(
                HabitLog(habitId = 2, dateString = todayStr)
            )
        )

        // Habit 1 should have its own 3-day streak
        assertEquals(3, habit1WithLogs.calculateCurrentStreak())
        assertEquals(3, habit1WithLogs.calculateLongestStreak())

        // Habit 2 should have its own independent 1-day streak
        assertEquals(1, habit2WithLogs.calculateCurrentStreak())
        assertEquals(1, habit2WithLogs.calculateLongestStreak())
    }

    @Test
    fun allHabitsMasterStreak_onlyCountsWhenAllHabitsAreCompleted() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val habit1 = Habit(id = 1, title = "Workout")
        val habit2 = Habit(id = 2, title = "Meditate")

        // Case 1: Yesterday both were completed, but today only habit 1 is completed
        val h1LogsPartial = listOf(
            HabitLog(habitId = 1, dateString = yesterdayStr),
            HabitLog(habitId = 1, dateString = todayStr)
        )
        val h2LogsPartial = listOf(
            HabitLog(habitId = 2, dateString = yesterdayStr)
        )

        val listPartial = listOf(
            HabitWithLogs(habit = habit1, logs = h1LogsPartial),
            HabitWithLogs(habit = habit2, logs = h2LogsPartial)
        )

        val (streakPartial, allDoneTodayPartial) = HabitWithLogs.calculateAllHabitsStreak(listPartial)
        // Since habit2 is NOT yet completed today, allDoneToday is false and streak stays at yesterday's count (1)
        assertFalse(allDoneTodayPartial)
        assertEquals(1, streakPartial)

        // Case 2: Now habit 2 is ALSO completed today!
        val h2LogsFull = listOf(
            HabitLog(habitId = 2, dateString = yesterdayStr),
            HabitLog(habitId = 2, dateString = todayStr)
        )
        val listFull = listOf(
            HabitWithLogs(habit = habit1, logs = h1LogsPartial),
            HabitWithLogs(habit = habit2, logs = h2LogsFull)
        )

        val (streakFull, allDoneTodayFull) = HabitWithLogs.calculateAllHabitsStreak(listFull)
        // Now both habits are completed today: streak counts up to 2!
        assertTrue(allDoneTodayFull)
        assertEquals(2, streakFull)
        assertEquals(2, HabitWithLogs.calculateAllHabitsRecordStreak(listFull))
    }
}
