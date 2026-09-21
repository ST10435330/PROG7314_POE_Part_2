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
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository, reloadKey) {
        loading = true
        loadError = null

        try {
            val loadedSubjects = repository.getSubjects()
            val loadedTasks = repository.getTasks()

            subjects = loadedSubjects
            tasks = loadedTasks
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
                    item { Text("No tasks yet. Add your first study task.") }
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
                            Text(if (task.completed) "Completed" else "Pending")
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
            onDismiss = {
                showForm = false
                saveError = null
            },
            onSave = { draft ->
                if (!saving) {
                    saving = true
                    saveError = null

                    scope.launch {
                        try {
                            val saved = repository.createTask(draft)
                            tasks = tasks + saved
                            showForm = false
                            Log.d("StudySync", "Task created")
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            saveError = if (exception is IllegalArgumentException) {
                                exception.message ?: "Check your task details."
                            } else {
                                "Could not save your task. Please try again."
                            }
                            Log.e("StudySync", "Task creation failed", exception)
                        } finally {
                            saving = false
                        }
                    }
                }
            }
        )
    }
}