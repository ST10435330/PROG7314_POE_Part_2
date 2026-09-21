package com.studysync.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppSettings(
    val defaultPriority: String = "MEDIUM",
    val defaultSort: TaskSort = TaskSort.DUE_DATE
)

class SettingsStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("studysync_settings", Context.MODE_PRIVATE)

    suspend fun load(): AppSettings = withContext(Dispatchers.IO) {
        val savedPriority = preferences.getString(
            "defaultPriority",
            "MEDIUM"
        )

        val priority = savedPriority
            ?.takeIf { it in listOf("HIGH", "MEDIUM", "LOW") }
            ?: "MEDIUM"

        val savedSort = preferences.getString(
            "defaultSort",
            TaskSort.DUE_DATE.name
        )

        val sort = TaskSort.entries.find { it.name == savedSort }
            ?: TaskSort.DUE_DATE

        AppSettings(
            defaultPriority = priority,
            defaultSort = sort
        )
    }

    suspend fun save(settings: AppSettings) {
        withContext(Dispatchers.IO) {
            require(settings.defaultPriority in listOf("HIGH", "MEDIUM", "LOW")) {
                "Choose a valid default priority."
            }

            val saved = preferences.edit()
                .putString("defaultPriority", settings.defaultPriority)
                .putString("defaultSort", settings.defaultSort.name)
                .commit()

            check(saved) {
                "Could not save your settings."
            }
        }
    }
}