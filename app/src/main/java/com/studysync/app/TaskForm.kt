package com.studysync.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun TaskForm(
    subjects: List<Subject>,
    saving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (StudyTask) -> Unit,
    initialTask: StudyTask? = null
) {
    var title by rememberSaveable(initialTask?.taskId) {
        mutableStateOf(initialTask?.title ?: "")
    }
    var description by rememberSaveable(initialTask?.taskId) {
        mutableStateOf(initialTask?.description ?: "")
    }
    var subjectId by rememberSaveable(initialTask?.taskId) {
        mutableStateOf(initialTask?.subjectId ?: "")
    }
    var dueDate by rememberSaveable(initialTask?.taskId) {
        mutableStateOf(initialTask?.dueDate ?: LocalDate.now().toString())
    }
    var priority by rememberSaveable(initialTask?.taskId) {
        mutableStateOf(initialTask?.priority ?: "MEDIUM")
    }
    var validationError by rememberSaveable(initialTask?.taskId) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!saving) onDismiss()
        },
        title = {
            Text(if (initialTask == null) "Add task" else "Edit task")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Task title") },
                    enabled = !saving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TaskChoice(
                    label = "Subject",
                    selectedText = subjects.find {
                        it.subjectId == subjectId
                    }?.name ?: "Choose a subject",
                    choices = subjects.map { it.subjectId to it.name },
                    enabled = !saving,
                    onSelect = {
                        subjectId = it
                        validationError = null
                    }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        validationError = null
                    },
                    label = { Text("Description (optional)") },
                    enabled = !saving,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {
                        dueDate = it
                        validationError = null
                    },
                    label = { Text("Due date") },
                    supportingText = { Text("YYYY-MM-DD, for example 2026-09-23") },
                    enabled = !saving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TaskChoice(
                    label = "Priority",
                    selectedText = priority.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    choices = listOf(
                        "HIGH" to "High",
                        "MEDIUM" to "Medium",
                        "LOW" to "Low"
                    ),
                    enabled = !saving,
                    onSelect = { priority = it }
                )

                (validationError ?: saveError)?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val task = (initialTask ?: StudyTask()).copy(
                        subjectId = subjectId,
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = dueDate.trim(),
                        priority = priority
                    )

                    validationError = TaskValidator.validate(task)

                    if (validationError == null) {
                        onSave(task)
                    }
                }
            ) {
                Text(if (saving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !saving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun TaskChoice(
    label: String,
    selectedText: String,
    choices: List<Pair<String, String>>,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedText)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                choices.forEach { (value, text) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}