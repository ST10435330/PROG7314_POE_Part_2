package com.studysync.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(repository: StudyRepository) {
    val scope = rememberCoroutineScope()

    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<StudyTask>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository, reloadKey) {
        loading = true
        loadError = null

        try {
            val loadedSubjects = repository.getSubjects()
            val loadedTasks = repository.getTasks()

            subjects = loadedSubjects
            tasks = loadedTasks

            // A previously selected task may have been removed.
            if (editingId != null &&
                loadedTasks.none { it.taskId == editingId }
            ) {
                editingId = null
                showForm = false
            }

            if (pendingDeleteId != null &&
                loadedTasks.none { it.taskId == pendingDeleteId }
            ) {
                pendingDeleteId = null
            }

            Log.d("StudySync", "Tasks and subjects loaded")
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            loadError = "Could not load your tasks. Please try again."
            Log.e("StudySync", "Task loading failed", exception)
        } finally {
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("My tasks", style = MaterialTheme.typography.headlineMedium)
        }

        when {
            loading -> {
                item { CircularProgressIndicator() }
            }

            loadError != null -> {
                item {
                    Text(
                        loadError ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { reloadKey++ }) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                item {
                    Button(
                        enabled = subjects.isNotEmpty() && !saving,
                        onClick = {
                            editingId = null
                            saveError = null
                            showForm = true
                        }
                    ) {
                        Text("Add task")
                    }
                }

                if (subjects.isEmpty()) {
                    item {
                        Text("Add a subject in the Subjects tab before creating a task.")
                    }
                } else if (tasks.isEmpty()) {
                    item {
                        Text("No tasks yet. Add your first study task.")
                    }
                }

                items(tasks, key = { it.taskId }) { task ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                subjects.find {
                                    it.subjectId == task.subjectId
                                }?.name ?: "Subject unavailable"
                            )

                            if (task.description.isNotBlank()) {
                                Text(task.description)
                            }

                            Text("Due: ${task.dueDate}")

                            Text(
                                "Priority: " + task.priority.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            )

                            Text(
                                if (task.completed) "Completed" else "Pending"
                            )

                            Row {
                                TextButton(
                                    enabled = !saving,
                                    onClick = {
                                        editingId = task.taskId
                                        saveError = null
                                        showForm = true
                                    }
                                ) {
                                    Text("Edit")
                                }

                                TextButton(
                                    enabled = !saving,
                                    onClick = {
                                        deleteError = null
                                        pendingDeleteId = task.taskId
                                    }
                                ) {
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm && !loading && loadError == null) {
        TaskForm(
            subjects = subjects,
            saving = saving,
            saveError = saveError,
            initialTask = tasks.find { it.taskId == editingId },
            onDismiss = {
                showForm = false
                editingId = null
                saveError = null
            },
            onSave = { draft ->
                if (!saving) {
                    saving = true
                    saveError = null
                    val creating = editingId == null

                    scope.launch {
                        try {
                            val saved = if (creating) {
                                repository.createTask(draft)
                            } else {
                                repository.updateTask(draft)
                            }

                            tasks = if (creating) {
                                tasks + saved
                            } else {
                                tasks.map {
                                    if (it.taskId == saved.taskId) saved else it
                                }
                            }

                            showForm = false
                            editingId = null

                            Log.d(
                                "StudySync",
                                if (creating) "Task created" else "Task edited"
                            )
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            saveError = if (exception is IllegalArgumentException) {
                                exception.message ?: "Check your task details."
                            } else {
                                "Could not save your task. Please try again."
                            }

                            Log.e("StudySync", "Task save failed", exception)
                        } finally {
                            saving = false
                        }
                    }
                }
            }
        )
    }

    val taskToDelete = tasks.find { it.taskId == pendingDeleteId }

    if (taskToDelete != null && !loading && loadError == null) {
        AlertDialog(
            onDismissRequest = {
                if (!saving) {
                    pendingDeleteId = null
                    deleteError = null
                }
            },
            title = { Text("Delete task?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Delete \"${taskToDelete.title}\"? This cannot be undone."
                    )

                    deleteError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        if (!saving) {
                            saving = true
                            deleteError = null

                            scope.launch {
                                try {
                                    repository.deleteTask(taskToDelete.taskId)

                                    tasks = tasks.filterNot {
                                        it.taskId == taskToDelete.taskId
                                    }

                                    pendingDeleteId = null
                                    Log.d("StudySync", "Task deleted")
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (exception: Exception) {
                                    deleteError =
                                        "Could not delete your task. Please try again."

                                    Log.e(
                                        "StudySync",
                                        "Task deletion failed",
                                        exception
                                    )
                                } finally {
                                    saving = false
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (saving) "Deleting..." else "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !saving,
                    onClick = {
                        pendingDeleteId = null
                        deleteError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}