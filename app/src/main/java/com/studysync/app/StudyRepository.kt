package com.studysync.app

interface StudyRepository {
    suspend fun getSubjects(): List<Subject>
    suspend fun createSubject(subject: Subject): Subject
    suspend fun updateSubject(subject: Subject): Subject
    suspend fun deleteSubject(subjectId: String)

    suspend fun getTasks(): List<StudyTask>
    suspend fun createTask(task: StudyTask): StudyTask
    suspend fun updateTask(task: StudyTask): StudyTask
    suspend fun deleteTask(taskId: String)
}