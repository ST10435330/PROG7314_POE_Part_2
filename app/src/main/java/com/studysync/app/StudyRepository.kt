package com.studysync.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

interface StudyRepository {
    suspend fun getSubjects(): List<Subject>
    suspend fun getTasks(): List<StudyTask>
    suspend fun createTask(task: StudyTask): StudyTask
    suspend fun updateTask(task: StudyTask): StudyTask
    suspend fun deleteTask(taskId: String)
}

// Temporary local implementation until the hosted API is connected.
class LocalStudyRepository(context: Context) : StudyRepository {
    private val subjectStorage = SubjectStorage(context.applicationContext)
    private val taskStorage = TaskStorage(context.applicationContext)

    override suspend fun getSubjects(): List<Subject> =
        withContext(Dispatchers.IO) {
            subjectStorage.load()
        }

    override suspend fun getTasks(): List<StudyTask> =
        withContext(Dispatchers.IO) {
            taskStorage.load()
        }

    override suspend fun createTask(task: StudyTask): StudyTask =
        withContext(Dispatchers.IO) {
            synchronized(SubjectStorage.writeLock) {
                val cleaned = validateTask(task)

                val saved = cleaned.copy(
                    taskId = UUID.randomUUID().toString(),
                    completed = false
                )

                val existing = taskStorage.load()
                taskStorage.save(existing + saved)
                saved
            }
        }

    override suspend fun updateTask(task: StudyTask): StudyTask =
        withContext(Dispatchers.IO) {
            synchronized(SubjectStorage.writeLock) {
                val existing = taskStorage.load()

                require(existing.any { it.taskId == task.taskId }) {
                    "This task no longer exists."
                }

                val updated = validateTask(task)

                taskStorage.save(
                    existing.map {
                        if (it.taskId == updated.taskId) updated else it
                    }
                )

                updated
            }
        }

    override suspend fun deleteTask(taskId: String) {
        withContext(Dispatchers.IO) {
            synchronized(SubjectStorage.writeLock) {
                val existing = taskStorage.load()

                require(existing.any { it.taskId == taskId }) {
                    "This task no longer exists."
                }

                taskStorage.save(
                    existing.filterNot { it.taskId == taskId }
                )
            }
        }
    }

    private fun validateTask(task: StudyTask): StudyTask {
        val cleaned = task.copy(
            title = task.title.trim(),
            description = task.description.trim(),
            dueDate = task.dueDate.trim()
        )

        val error = TaskValidator.validate(cleaned)

        require(error == null) {
            error ?: "Invalid task."
        }

        require(subjectStorage.load().any {
            it.subjectId == cleaned.subjectId
        }) {
            "That subject is no longer available. Reload and try again."
        }

        return cleaned
    }
}