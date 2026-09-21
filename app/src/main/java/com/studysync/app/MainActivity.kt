package com.studysync.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("StudySync", "Main screen created")

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2855B8)
                )
            ) {
                StudySyncScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySyncScreen() {
    val context = LocalContext.current
    val repository: StudyRepository = remember(context) {
        LocalStudyRepository(context.applicationContext)
    }
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StudySync") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tasks") }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Subjects") }
                )
            }

            if (selectedTab == 0) {
                TasksScreen(repository)
            } else {
                SubjectsScreen()
            }
        }
    }
}