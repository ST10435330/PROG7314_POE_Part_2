package com.studysync.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

interface StudyRepository {
    suspend fun getSubjects(): List<Subject>
    suspend fun getTasks(): List<StudyTask>
    suspend fun createTask(task: StudyTask): StudyTask
}

// Temporary implementation while the hosted API is being developed.
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
                val cleaned = task.copy(
                    title = task.title.trim(),
                    description = task.description.trim()
                )

                val validationError = TaskValidator.validate(cleaned)
                require(validationError == null) {
                    validationError ?: "Invalid task."
                }

                require(subjectStorage.load().any {
                    it.subjectId == cleaned.subjectId
                }) {
                    "That subject is no longer available. Reload and try again."
                }

                val saved = cleaned.copy(
                    taskId = UUID.randomUUID().toString(),
                    completed = false
                )

                val existing = taskStorage.load()
                taskStorage.save(existing + saved)
                saved
            }
        }
}