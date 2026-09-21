package com.studysync.app

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TaskValidatorTest {

    private fun validTask() = StudyTask(
        subjectId = "subject-1",
        title = "Complete assignment",
        description = "Finish the research section",
        dueDate = "2026-09-23",
        priority = "HIGH"
    )

    @Test
    fun validTaskIsAccepted() {
        assertNull(TaskValidator.validate(validTask()))
    }

    @Test
    fun blankTitleIsRejected() {
        val task = validTask().copy(title = "   ")

        assertNotNull(TaskValidator.validate(task))
    }

    @Test
    fun missingSubjectIsRejected() {
        val task = validTask().copy(subjectId = "")

        assertNotNull(TaskValidator.validate(task))
    }

    @Test
    fun titleLengthLimitIsEnforced() {
        val atLimit = validTask().copy(title = "A".repeat(120))
        val overLimit = validTask().copy(title = "A".repeat(121))

        assertNull(TaskValidator.validate(atLimit))
        assertNotNull(TaskValidator.validate(overLimit))
    }

    @Test
    fun descriptionLengthLimitIsEnforced() {
        val atLimit = validTask().copy(
            description = "A".repeat(2000)
        )
        val overLimit = validTask().copy(
            description = "A".repeat(2001)
        )

        assertNull(TaskValidator.validate(atLimit))
        assertNotNull(TaskValidator.validate(overLimit))
    }

    @Test
    fun impossibleDatesAndWrongFormatAreRejected() {
        val invalidDates = listOf(
            "2026-02-30",
            "2026-02-29",
            "2026-13-01",
            "23/09/2026",
            ""
        )

        for (date in invalidDates) {
            val task = validTask().copy(dueDate = date)

            assertNotNull(
                "Expected $date to be rejected",
                TaskValidator.validate(task)
            )
        }
    }

    @Test
    fun validLeapDayAndPastDeadlineAreAccepted() {
        val leapDay = validTask().copy(dueDate = "2024-02-29")
        val pastDeadline = validTask().copy(dueDate = "2020-01-01")

        assertNull(TaskValidator.validate(leapDay))
        assertNull(TaskValidator.validate(pastDeadline))
    }

    @Test
    fun unsupportedPriorityIsRejected() {
        val task = validTask().copy(priority = "URGENT")

        assertNotNull(TaskValidator.validate(task))
    }
}