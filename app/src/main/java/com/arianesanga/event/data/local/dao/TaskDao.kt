package com.arianesanga.event.data.local.dao

import androidx.room.*
import com.arianesanga.event.data.local.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM task WHERE event_id = :eventId ORDER BY id DESC")
    suspend fun getTasksByEvent(eventId: Int): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT COUNT(*) FROM task WHERE event_id = :eventId AND status = :status")
    suspend fun countByStatus(eventId: Int, status: Int): Int

    @Query("SELECT IFNULL(SUM(value), 0) FROM task WHERE event_id = :eventId AND status = :status")
    suspend fun sumValueByStatus(eventId: Int, status: Int): Double

    @Query("SELECT IFNULL(SUM(value), 0) FROM task WHERE event_id = :eventId")
    suspend fun sumAllValues(eventId: Int): Double
}