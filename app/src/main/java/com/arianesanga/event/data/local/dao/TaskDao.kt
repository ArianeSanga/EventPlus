package com.arianesanga.event.data.local.dao

import androidx.room.*
import com.arianesanga.event.data.local.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM task")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM task WHERE event_id = :eventId")
    suspend fun getTasksByEvent(eventId: Int): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}