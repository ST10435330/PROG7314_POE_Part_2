package com.studysync.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("StudySync", "Main screen created")

        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2855B8))) {
                StudySyncScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySyncScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("StudySync") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("My tasks", style = MaterialTheme.typography.headlineMedium)
            Text("Keep your subjects, deadlines and study tasks in one place.")
            HorizontalDivider()
            Text("Your task list is empty.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
