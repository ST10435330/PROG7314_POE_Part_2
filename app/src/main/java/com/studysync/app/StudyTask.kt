package com.studysync.app

//information stored for each task
data class StudyTask(
    val taskId: String = "",
    val subjectId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val priority: String = "MEDIUM",
    val completed: Boolean = false
)