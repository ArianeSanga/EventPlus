package com.arianesanga.event.data.repository

import com.arianesanga.event.data.local.dao.TaskDao
import com.arianesanga.event.data.local.model.Task
import com.arianesanga.event.data.remote.repository.TaskRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val remoteId = generatedId.toString()

        val data = mutableMapOf<String, Any>()
        data["id"] = generatedId
        data["eventId"] = task.eventId
        data["title"] = task.title
        task.description?.let { data["description"] = it }
        task.deadline?.let { data["deadline"] = it }
        data["isCompleted"] = task.isCompleted

        remote.createOrUpdateTask(remoteId, data) { _, _ -> }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        local.updateTask(task)

        val data = mutableMapOf<String, Any>()
        data["title"] = task.title
        task.description?.let { data["description"] = it }
        task.deadline?.let { data["deadline"] = it }
        data["isCompleted"] = task.isCompleted

        remote.createOrUpdateTask(task.id.toString(), data) { _, _ -> }
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        local.deleteTask(task)
        remote.deleteTask(task.id.toString()) { _ -> }
    }
}