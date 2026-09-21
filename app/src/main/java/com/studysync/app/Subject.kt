package com.studysync.app

data class Subject(
    val subjectId: String,
    val name: String,
    val lecturerName: String = ""
)

object SubjectValidator {

    fun validate(
        name: String,
        lecturerName: String,
        existingSubjects: List<Subject>,
        editingSubjectId: String? = null
    ): String? {
        val cleanName = name.trim()

        if (cleanName.isEmpty()) {
            return "Enter a subject name."
        }

        if (cleanName.length > 80) {
            return "Subject names must be 80 characters or fewer."
        }

        if (lecturerName.trim().length > 80) {
            return "Lecturer names must be 80 characters or fewer."
        }

        val duplicateExists = existingSubjects.any {
            it.subjectId != editingSubjectId &&
                    it.name.equals(cleanName, ignoreCase = true)
        }

        if (duplicateExists) {
            return "That subject already exists."
        }

        return null
    }
}