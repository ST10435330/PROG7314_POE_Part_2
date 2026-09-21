package com.studysync.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskProgressPanel(
    tasks: List<StudyTask>,
    subjects: List<Subject>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val overall = TaskLogic.progress(tasks)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Study progress",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "${overall.completed} of ${overall.total} tasks completed" +
                        " · ${overall.percentage}%"
            )

            LinearProgressIndicator(
                progress = { overall.fraction },
                modifier = Modifier.fillMaxWidth()
            )

            if (subjects.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) "Hide subject progress"
                        else "Show subject progress"
                    )
                }
            }

            if (expanded) {
                subjects.sortedBy { it.name.lowercase() }.forEach { subject ->
                    val progress = TaskLogic.progress(tasks, subject.subjectId)

                    Text(
                        subject.name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        "${progress.completed}/${progress.total} completed" +
                                " · ${progress.percentage}%"
                    )

                    LinearProgressIndicator(
                        progress = { progress.fraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TaskFiltersDialog(
    subjects: List<Subject>,
    selectedSubjectId: String,
    filter: TaskFilter,
    sort: TaskSort,
    onSubjectChange: (String) -> Unit,
    onFilterChange: (TaskFilter) -> Unit,
    onSortChange: (TaskSort) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter and sort") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaskChoice(
                    label = "Subject",
                    selectedText = subjects.find {
                        it.subjectId == selectedSubjectId
                    }?.name ?: "All subjects",
                    choices = listOf("" to "All subjects") +
                            subjects.map { it.subjectId to it.name },
                    enabled = true,
                    onSelect = onSubjectChange
                )

                TaskChoice(
                    label = "Status",
                    selectedText = filter.label,
                    choices = TaskFilter.entries.map { it.name to it.label },
                    enabled = true,
                    onSelect = {
                        onFilterChange(TaskFilter.valueOf(it))
                    }
                )

                TaskChoice(
                    label = "Sort order",
                    selectedText = sort.label,
                    choices = TaskSort.entries.map { it.name to it.label },
                    enabled = true,
                    onSelect = {
                        onSortChange(TaskSort.valueOf(it))
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("Reset")
            }
        }
    )
}