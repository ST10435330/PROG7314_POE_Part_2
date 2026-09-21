package com.studysync.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TaskStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("studysync", Context.MODE_PRIVATE)

    fun load(): List<StudyTask> {
        val array = JSONArray(preferences.getString("tasks", "[]") ?: "[]")

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)

            StudyTask(
                taskId = item.getString("taskId"),
                subjectId = item.getString("subjectId"),
                title = item.getString("title"),
                description = item.getString("description"),
                dueDate = item.getString("dueDate"),
                priority = item.getString("priority"),
                completed = item.getBoolean("completed")
            )
        }
    }

    fun save(tasks: List<StudyTask>) {
        val array = JSONArray()

        tasks.forEach { task ->
            array.put(JSONObject().apply {
                put("taskId", task.taskId)
                put("subjectId", task.subjectId)
                put("title", task.title)
                put("description", task.description)
                put("dueDate", task.dueDate)
                put("priority", task.priority)
                put("completed", task.completed)
            })
        }

        preferences.edit().putString("tasks", array.toString()).apply()
    }
}