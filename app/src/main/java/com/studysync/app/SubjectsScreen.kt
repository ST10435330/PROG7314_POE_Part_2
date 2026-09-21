package com.studysync.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun SubjectsScreen() {
    val context = LocalContext.current
    val storage = remember { SubjectStorage(context) }

    val initialLoad = remember {
        runCatching { storage.load() }
    }

    var subjects by remember {
        mutableStateOf(initialLoad.getOrDefault(emptyList()))
    }

    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var name by rememberSaveable { mutableStateOf("") }
    var lecturerName by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var screenError by remember {
        mutableStateOf<String?>(null)
    }

    fun saveSubjects(updated: List<Subject>): Boolean {
        return try {
            val sorted = updated.sortedBy { it.name.lowercase() }

            storage.save(sorted)
            subjects = sorted
            screenError = null
            true
        } catch (exception: Exception) {
            screenError = if (exception is IllegalArgumentException) {
                exception.message ?: "Could not save your changes."
            } else {
                "Could not save your changes. Try again."
            }
            Log.e("StudySync", "Subject save failed", exception)
            false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My subjects",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Manage the modules linked to your study tasks.")
        }

        if (initialLoad.isFailure) {
            item {
                Text(
                    text = "Saved subjects could not be loaded. Restart the app and try again.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        screenError?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Button(
                enabled = initialLoad.isSuccess,
                onClick = {
                    editingId = null
                    name = ""
                    lecturerName = ""
                    formError = null
                    screenError = null
                    showForm = true
                }
            ) {
                Text("Add subject")
            }
        }

        if (subjects.isEmpty() && initialLoad.isSuccess) {
            item {
                Text("No subjects yet.")
            }
        }

        items(subjects, key = { it.subjectId }) { subject ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (subject.lecturerName.isNotBlank()) {
                        Text(subject.lecturerName)
                    }

                    Row {
                        TextButton(
                            onClick = {
                                editingId = subject.subjectId
                                name = subject.name
                                lecturerName = subject.lecturerName
                                formError = null
                                screenError = null
                                showForm = true
                            }
                        ) {
                            Text("Edit")
                        }

                        TextButton(
                            onClick = {
                                screenError = null
                                pendingDeleteId = subject.subjectId
                            }
                        ) {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = {
                Text(
                    if (editingId == null) "Add subject"
                    else "Edit subject"
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            formError = null
                        },
                        label = { Text("Subject name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lecturerName,
                        onValueChange = {
                            lecturerName = it
                            formError = null
                        },
                        label = { Text("Lecturer name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    formError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        formError = SubjectValidator.validate(
                            name = name,
                            lecturerName = lecturerName,
                            existingSubjects = subjects,
                            editingSubjectId = editingId
                        )

                        if (formError == null) {
                            val subject = Subject(
                                subjectId = editingId
                                    ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                lecturerName = lecturerName.trim()
                            )

                            val updated = if (editingId == null) {
                                subjects + subject
                            } else {
                                subjects.map {
                                    if (it.subjectId == editingId) subject
                                    else it
                                }
                            }

                            if (saveSubjects(updated)) {
                                Log.d(
                                    "StudySync",
                                    if (editingId == null) "Subject added"
                                    else "Subject edited"
                                )
                                showForm = false
                            } else {
                                formError = "Could not save. Try again."
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val subjectToDelete = subjects.find {
        it.subjectId == pendingDeleteId
    }

    if (subjectToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete subject?") },
            text = {
                Text(
                    "Delete ${subjectToDelete.name}? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = subjects.filterNot {
                            it.subjectId == subjectToDelete.subjectId
                        }

                        if (saveSubjects(updated)) {
                            Log.d("StudySync", "Subject deleted")
                        }

                        pendingDeleteId = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}