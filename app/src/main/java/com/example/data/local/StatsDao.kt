package com.example.data.local

import androidx.room.*
import com.example.data.model.UserProductivityStats
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM user_productivity_stats WHERE id = 1 LIMIT 1")
    fun getStats(): Flow<UserProductivityStats?>

    @Query("SELECT * FROM user_productivity_stats WHERE id = 1 LIMIT 1")
    suspend fun getStatsDirect(): UserProductivityStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserProductivityStats)

    @Query("UPDATE user_productivity_stats SET totalXp = totalXp + :xpGain WHERE id = 1")
    suspend fun addXp(xpGain: Int)

    @Query("UPDATE user_productivity_stats SET tasksCompletedCount = tasksCompletedCount + 1, totalXp = totalXp + :xpGain WHERE id = 1")
    suspend fun recordTaskCompleted(xpGain: Int = 15)

    @Query("UPDATE user_productivity_stats SET habitsCompletedCount = habitsCompletedCount + 1, totalXp = totalXp + :xpGain WHERE id = 1")
    suspend fun recordHabitCompleted(xpGain: Int = 20)

    @Query("UPDATE user_productivity_stats SET focusMinutesTotal = focusMinutesTotal + :minutes, focusSessionsCompleted = focusSessionsCompleted + 1, totalXp = totalXp + :xpGain WHERE id = 1")
    suspend fun recordFocusSession(minutes: Int, xpGain: Int = 30)
}
