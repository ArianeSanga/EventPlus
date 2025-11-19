package com.arianesanga.event.data.repository

import com.arianesanga.event.data.local.dao.TaskDao
import com.arianesanga.event.data.local.model.Task
import com.arianesanga.event.data.remote.repository.TaskRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TaskStats(
    val pendingCount: Int,
    val inProgressCount: Int,
    val completedCount: Int,
    val committedValue: Double,
    val completedValue: Double
)

class TaskRepository(
    private val local: TaskDao,
    private val remote: TaskRemoteRepository
) {

    suspend fun getTasksByEvent(eventId: Int): List<Task> = withContext(Dispatchers.IO) {
        local.getTasksByEvent(eventId)
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        val rowId = local.insertTask(task)
        val generatedId = rowId.toInt()


        val data = mutableMapOf<String, Any>()
        data["id"] = generatedId
        data["eventId"] = task.eventId
        data["title"] = task.title
        task.description?.let { data["description"] = it }
        data["value"] = task.value
        data["status"] = task.status

        remote.createOrUpdateTask(generatedId.toString(), data) { _, _ -> }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        local.updateTask(task)

        val data = mutableMapOf<String, Any>()
        data["id"] = task.id
        data["eventId"] = task.eventId
        data["title"] = task.title
        task.description?.let { data["description"] = it }
        data["value"] = task.value
        data["status"] = task.status

        remote.createOrUpdateTask(task.id.toString(), data) { _, _ -> }
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        local.deleteTask(task)
        remote.deleteTask(task.id.toString()) { _ -> }
    }

    suspend fun getStatsForEvent(eventId: Int): TaskStats = withContext(Dispatchers.IO) {
        val pending = local.countByStatus(eventId, 0)
        val inProgress = local.countByStatus(eventId, 1)
        val completed = local.countByStatus(eventId, 2)

        val sumPending = local.sumValueByStatus(eventId, 0)
        val sumInProgress = local.sumValueByStatus(eventId, 1)
        val sumCompleted = local.sumValueByStatus(eventId, 2)

        val committed = sumPending + sumInProgress + sumCompleted

        TaskStats(
            pendingCount = pending,
            inProgressCount = inProgress,
            completedCount = completed,
            committedValue = committed,
            completedValue = sumCompleted
        )
    }
}