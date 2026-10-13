package com.example.data.local

import androidx.room.*
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE listId = :listId AND parentTaskId IS NULL ORDER BY orderIndex ASC, id DESC")
    fun getTasksForList(listId: Long): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isStarred = 1 AND parentTaskId IS NULL ORDER BY orderIndex ASC, id DESC")
    fun getStarredTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND parentTaskId IS NULL ORDER BY completedAt DESC, id DESC")
    fun getAllCompletedTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE parentTaskId IS NULL ORDER BY orderIndex ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskWithSubtasksById(id: Long): Flow<TaskWithSubtasks?>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): Task?

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (dueDateMillis IS NULL OR dueDateMillis <= :endOfDayMillis) ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, dueDateMillis ASC, id DESC")
    suspend fun getPendingTasksForWidget(endOfDayMillis: Long): List<TaskWithSubtasks>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND completedAt >= :startOfDayMillis")
    suspend fun getCompletedTodayCount(startOfDayMillis: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND (dueDateMillis IS NULL OR dueDateMillis <= :endOfDayMillis)")
    suspend fun getPendingTodayCount(endOfDayMillis: Long): Int

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY orderIndex ASC, id ASC")
    suspend fun getSubtasksForTaskId(taskId: Long): List<Subtask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteTasksByIds(taskIds: List<Long>)

    @Query("UPDATE tasks SET listId = :targetListId WHERE id IN (:taskIds)")
    suspend fun moveTasksToList(taskIds: List<Long>, targetListId: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET isStarred = :isStarred WHERE id = :id")
    suspend fun setTaskStarred(id: Long, isStarred: Boolean)

    @Query("DELETE FROM tasks WHERE listId = :listId AND isCompleted = 1")
    suspend fun deleteCompletedTasksForList(listId: Long)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllCompletedTasks()

    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND completedAt IS NOT NULL AND completedAt <= :thresholdTimestamp")
    suspend fun deleteCompletedTasksOlderThan(thresholdTimestamp: Long)

    // Recurring Task Schedules (Daily, Weekly, Monthly, Yearly)
    @Transaction
    @Query("SELECT * FROM tasks WHERE recurrenceFrequency != 'NONE' AND isCompleted = 0 AND parentTaskId IS NULL ORDER BY dueDateMillis ASC, id DESC")
    fun getActiveRecurringTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE recurrenceFrequency = :frequency AND isCompleted = 0 AND parentTaskId IS NULL ORDER BY dueDateMillis ASC, id DESC")
    fun getRecurringTasksByFrequency(frequency: com.example.data.model.RecurrenceFrequency): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE listId = :listId AND recurrenceFrequency != 'NONE' AND isCompleted = 0 AND parentTaskId IS NULL ORDER BY dueDateMillis ASC, id DESC")
    fun getRecurringTasksForList(listId: Long): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE recurrenceFrequency != 'NONE' AND dueDateMillis BETWEEN :startMillis AND :endMillis AND isCompleted = 0 AND parentTaskId IS NULL ORDER BY dueDateMillis ASC")
    fun getRecurringTasksDueBetween(startMillis: Long, endMillis: Long): Flow<List<TaskWithSubtasks>>

    @Query("UPDATE tasks SET spawnedNextTaskId = :spawnedId WHERE id = :id")
    suspend fun setSpawnedNextTaskId(id: Long, spawnedId: Long?)

    @Query("DELETE FROM tasks WHERE id = :id AND isCompleted = 0")
    suspend fun deleteUncompletedTaskById(id: Long)

    // Subtasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<Subtask>)

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtaskById(id: Long)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setSubtaskCompleted(id: Long, isCompleted: Boolean)
}
