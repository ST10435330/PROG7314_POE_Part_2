package com.studysync.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SubjectStorage(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences("studysync", Context.MODE_PRIVATE)

    fun load(): List<Subject> {
        val saved = preferences.getString("subjects", "[]") ?: "[]"
        val array = JSONArray(saved)

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
        val array = JSONArray()

        subjects.forEach { subject ->
            val item = JSONObject().apply {
                put("subjectId", subject.subjectId)
                put("name", subject.name)
                put("lecturerName", subject.lecturerName)
            }

            array.put(item)
        }

        preferences.edit()
            .putString("subjects", array.toString())
            .apply()
    }
}