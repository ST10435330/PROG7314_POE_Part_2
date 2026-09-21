package com.studysync.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SubjectStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("studysync", Context.MODE_PRIVATE)

    private val taskStorage = TaskStorage(context)

    internal companion object {
        val writeLock = Any()
    }

    fun load(): List<Subject> {
        val array = JSONArray(preferences.getString("subjects", "[]") ?: "[]")

        return List(array.length()) { index ->
            val item = array.getJSONObject(index)

            Subject(
                subjectId = item.getString("subjectId"),
                name = item.getString("name"),
                lecturerName = item.optString("lecturerName", "")
            )
        }
    }

    fun save(subjects: List<Subject>) {
        synchronized(writeLock) {
            val remainingIds = subjects.map { it.subjectId }.toSet()
            val removedIds = load()
                .map { it.subjectId }
                .filterNot { it in remainingIds }
                .toSet()

            if (removedIds.isNotEmpty()) {
                val hasLinkedTasks = taskStorage.load().any {
                    it.subjectId in removedIds
                }

                require(!hasLinkedTasks) {
                    "Delete or move this subject's tasks first."
                }
            }

            val array = JSONArray()

            subjects.forEach { subject ->
                array.put(JSONObject().apply {
                    put("subjectId", subject.subjectId)
                    put("name", subject.name)
                    put("lecturerName", subject.lecturerName)
                })
            }

            preferences.edit().putString("subjects", array.toString()).apply()
        }
    }
}