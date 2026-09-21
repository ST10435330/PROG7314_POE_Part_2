package com.studysync.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    var name by rememberSaveable { mutableStateOf("") }
    var lecturerName by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
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
            Text("Add your modules to organise your study tasks.")
        }

        if (initialLoad.isFailure) {
            item {
                Text(
                    text = "Saved subjects could not be loaded. Restart the app and try again.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    error = null
                },
                label = { Text("Subject name") },
                placeholder = { Text("e.g. PROG7314") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = lecturerName,
                onValueChange = {
                    lecturerName = it
                    error = null
                },
                label = { Text("Lecturer name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        error?.let { message ->
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
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    error = SubjectValidator.validate(
                        name,
                        lecturerName,
                        subjects
                    )

                    if (error == null) {
                        val newSubject = Subject(
                            subjectId = UUID.randomUUID().toString(),
                            name = name.trim(),
                            lecturerName = lecturerName.trim()
                        )

                        val updatedSubjects = (subjects + newSubject)
                            .sortedBy { it.name.lowercase() }

                        runCatching {
                            storage.save(updatedSubjects)
                        }.onSuccess {
                            subjects = updatedSubjects
                            name = ""
                            lecturerName = ""
                            Log.d("StudySync", "Subject added")
                        }.onFailure {
                            error = "Could not save the subject. Try again."
                            Log.e("StudySync", "Subject save failed", it)
                        }
                    }
                }
            ) {
                Text("Add subject")
            }
        }

        item {
            HorizontalDivider()
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (subject.lecturerName.isNotBlank()) {
                        Text(
                            text = subject.lecturerName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}