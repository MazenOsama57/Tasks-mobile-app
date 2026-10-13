package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE completedAt >= :sinceMillis ORDER BY completedAt DESC")
    fun getSessionsSince(sinceMillis: Long): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getSessionsForTaskId(taskId: Long): Flow<List<FocusSession>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE completedAt >= :sinceMillis")
    fun getFocusMinutesSince(sinceMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}
