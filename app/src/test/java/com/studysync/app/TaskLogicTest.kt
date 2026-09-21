package com.studysync.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TaskLogicTest {
    private val today = LocalDate.of(2026, 9, 21)

    private fun task(
        id: String,
        subject: String = "subject-1",
        due: String = "2026-09-21",
        priority: String = "MEDIUM",
        completed: Boolean = false,
        title: String = id
    ) = StudyTask(
        taskId = id,
        subjectId = subject,
        title = title,
        dueDate = due,
        priority = priority,
        completed = completed
    )

    @Test
    fun overdueRequiresAnUnfinishedTaskBeforeToday() {
        assertTrue(
            TaskLogic.isOverdue(task("past", due = "2026-09-20"), today)
        )
        assertFalse(
            TaskLogic.isOverdue(task("today"), today)
        )
        assertFalse(
            TaskLogic.isOverdue(task("future", due = "2026-09-22"), today)
        )
        assertFalse(
            TaskLogic.isOverdue(
                task("done", due = "2026-09-20", completed = true),
                today
            )
        )
    }

    @Test
    fun emptyProgressIsZero() {
        val result = TaskLogic.progress(emptyList())

        assertEquals(0, result.total)
        assertEquals(0, result.completed)
        assertEquals(0, result.percentage)
        assertEquals(0f, result.fraction, 0.001f)
    }

    @Test
    fun completionAndReopeningChangeProgress() {
        val tasks = listOf(
            task("one", completed = true),
            task("two")
        )

        assertEquals(50, TaskLogic.progress(tasks).percentage)

        val completed = tasks.map { it.copy(completed = true) }
        assertEquals(100, TaskLogic.progress(completed).percentage)

        val reopened = completed.map { it.copy(completed = false) }
        assertEquals(0, TaskLogic.progress(reopened).percentage)
    }

    @Test
    fun subjectProgressExcludesOtherSubjects() {
        val tasks = listOf(
            task("one", subject = "a", completed = true),
            task("two", subject = "a"),
            task("three", subject = "b", completed = true)
        )

        val result = TaskLogic.progress(tasks, "a")

        assertEquals(2, result.total)
        assertEquals(1, result.completed)
        assertEquals(50, result.percentage)
        assertEquals(0, TaskLogic.progress(tasks, "missing").total)
    }

    @Test
    fun subjectAndStatusFiltersWorkTogether() {
        val tasks = listOf(
            task("a-pending", subject = "a"),
            task("a-done", subject = "a", completed = true),
            task("b-pending", subject = "b")
        )

        val pending = TaskLogic.select(
            tasks, TaskFilter.PENDING, TaskSort.DUE_DATE, "a", today
        )
        val completed = TaskLogic.select(
            tasks, TaskFilter.COMPLETED, TaskSort.DUE_DATE, "a", today
        )

        assertEquals(listOf("a-pending"), pending.map { it.taskId })
        assertEquals(listOf("a-done"), completed.map { it.taskId })
    }

    @Test
    fun overdueFilterExcludesTodayAndCompletedTasks() {
        val tasks = listOf(
            task("late", due = "2026-09-20"),
            task("today"),
            task("finished", due = "2026-09-19", completed = true)
        )

        val result = TaskLogic.select(
            tasks, TaskFilter.OVERDUE, TaskSort.DUE_DATE, "", today
        )

        assertEquals(listOf("late"), result.map { it.taskId })
    }

    @Test
    fun prioritySortUsesDeadlineToBreakTies() {
        val tasks = listOf(
            task("low", priority = "LOW"),
            task("high-later", priority = "HIGH", due = "2026-09-25"),
            task("medium", priority = "MEDIUM"),
            task("high-earlier", priority = "HIGH", due = "2026-09-22")
        )

        val result = TaskLogic.select(
            tasks, TaskFilter.ALL, TaskSort.PRIORITY, "", today
        )

        assertEquals(
            listOf("high-earlier", "high-later", "medium", "low"),
            result.map { it.taskId }
        )
    }

    @Test
    fun deadlineAndTitleSortUseTheExpectedOrder() {
        val tasks = listOf(
            task("a", title = "zebra", due = "2026-09-25"),
            task("b", title = "Alpha", due = "2026-09-23"),
            task("c", title = "beta", due = "2026-09-20")
        )

        val byDate = TaskLogic.select(
            tasks, TaskFilter.ALL, TaskSort.DUE_DATE, "", today
        )
        val byTitle = TaskLogic.select(
            tasks, TaskFilter.ALL, TaskSort.TITLE, "", today
        )

        assertEquals(listOf("c", "b", "a"), byDate.map { it.taskId })
        assertEquals(listOf("b", "c", "a"), byTitle.map { it.taskId })
    }
}