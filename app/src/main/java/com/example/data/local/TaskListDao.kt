package com.example.data.local

import androidx.room.*
import com.example.data.model.TaskList
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists ORDER BY orderIndex ASC, id ASC")
    fun getAllLists(): Flow<List<TaskList>>

    @Query("SELECT * FROM task_lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: Long): TaskList?

    @Query("SELECT COUNT(*) FROM task_lists")
    suspend fun getListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskList): Long

    @Update
    suspend fun updateList(list: TaskList)

    @Delete
    suspend fun deleteList(list: TaskList)

    @Query("DELETE FROM task_lists WHERE id = :id")
    suspend fun deleteListById(id: Long)
}
