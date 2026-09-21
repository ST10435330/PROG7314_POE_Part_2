package com.studysync.app

import java.time.LocalDate
import java.time.format.DateTimeParseException

object TaskValidator {

    // A null result means the task passed validation.
    fun validate(task: StudyTask): String? {
        if (task.title.isBlank()) {
            return "Enter a task title."
        }

        if (task.title.trim().length > 120) {
            return "The title must be 120 characters or fewer."
        }

        if (task.subjectId.isBlank()) {
            return "Choose a subject."
        }

        if (task.description.trim().length > 2000) {
            return "The description must be 2000 characters or fewer."
        }

        if (!isValidDate(task.dueDate)) {
            return "Enter a valid date in YYYY-MM-DD format."
        }

        if (task.priority !in listOf("HIGH", "MEDIUM", "LOW")) {
            return "Choose High, Medium or Low priority."
        }

        return null
    }

    private fun isValidDate(value: String): Boolean {
        if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return false
        }

        return try {
            val date = LocalDate.parse(value)
            date.year in 1900..2100
        } catch (exception: DateTimeParseException) {
            false
        }
    }
}