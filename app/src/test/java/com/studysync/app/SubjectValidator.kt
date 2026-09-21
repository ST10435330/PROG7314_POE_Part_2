package com.studysync.app

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SubjectValidatorTest {

    @Test
    fun validSubjectWithoutLecturerIsAccepted() {
        assertNull(
            SubjectValidator.validate("PROG7314", "", emptyList())
        )
    }

    @Test
    fun blankSubjectNameIsRejected() {
        assertNotNull(
            SubjectValidator.validate("   ", "", emptyList())
        )
    }

    @Test
    fun subjectNameLengthLimitIsEnforced() {
        assertNull(
            SubjectValidator.validate("A".repeat(80), "", emptyList())
        )

        assertNotNull(
            SubjectValidator.validate("A".repeat(81), "", emptyList())
        )
    }

    @Test
    fun lecturerNameLengthLimitIsEnforced() {
        assertNull(
            SubjectValidator.validate(
                "PROG7314", "A".repeat(80), emptyList()
            )
        )

        assertNotNull(
            SubjectValidator.validate(
                "PROG7314", "A".repeat(81), emptyList()
            )
        )
    }

    @Test
    fun duplicateNameIgnoresCaseAndSurroundingSpaces() {
        val existing = listOf(
            Subject(subjectId = "1", name = "PROG7314")
        )

        assertNotNull(
            SubjectValidator.validate(" prog7314 ", "", existing)
        )
    }

    @Test
    fun editingSubjectCanKeepItsOwnName() {
        val existing = listOf(
            Subject(subjectId = "1", name = "PROG7314")
        )

        assertNull(
            SubjectValidator.validate(
                name = "PROG7314",
                lecturerName = "Updated lecturer",
                existingSubjects = existing,
                editingSubjectId = "1"
            )
        )
    }

    @Test
    fun editingCannotUseAnotherSubjectsName() {
        val existing = listOf(
            Subject(subjectId = "1", name = "PROG7314"),
            Subject(subjectId = "2", name = "INSY7314")
        )

        assertNotNull(
            SubjectValidator.validate(
                name = " insy7314 ",
                lecturerName = "",
                existingSubjects = existing,
                editingSubjectId = "1"
            )
        )
    }
}