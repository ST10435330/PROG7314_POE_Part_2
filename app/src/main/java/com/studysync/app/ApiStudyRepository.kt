package com.studysync.app

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class ApiException(message: String) : Exception(message)

fun apiErrorMessage(
    exception: Exception,
    fallback: String
): String {
    return when (exception) {
        is ApiException,
        is IllegalArgumentException -> exception.message ?: fallback

        else -> fallback
    }
}

class ApiStudyRepository(
    private val userId: String,
    private val api: StudyApi = StudyApiClient.service
) : StudyRepository {

    override suspend fun getSubjects(): List<Subject> {
        return body { api.getSubjects(it) }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun createSubject(subject: Subject): Subject {
        return body {
            api.createSubject(it, subject.toRequest())
        }
    }

    override suspend fun updateSubject(subject: Subject): Subject {
        return body {
            api.updateSubject(
                it,
                subject.subjectId,
                subject.toRequest()
            )
        }
    }

    override suspend fun deleteSubject(subjectId: String) {
        execute {
            api.deleteSubject(it, subjectId)
        }
    }

    override suspend fun getTasks(): List<StudyTask> {
        return body { api.getTasks(it) }
    }

    override suspend fun createTask(task: StudyTask): StudyTask {
        return body {
            api.createTask(it, task.toRequest())
        }
    }

    override suspend fun updateTask(task: StudyTask): StudyTask {
        return body {
            api.updateTask(
                it,
                task.taskId,
                task.toRequest()
            )
        }
    }

    override suspend fun deleteTask(taskId: String) {
        execute {
            api.deleteTask(it, taskId)
        }
    }

    private suspend fun authorization(forceRefresh: Boolean): String {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null || user.uid != userId) {
            throw ApiException("Please sign in again.")
        }

        val token = try {
            user.getIdToken(forceRefresh).await().token
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: FirebaseNetworkException) {
            throw ApiException(
                "Could not refresh your session. Check your internet connection."
            )
        } catch (exception: Exception) {
            throw ApiException(
                "Your session could not be verified. Sign out and sign in again."
            )
        }

        if (auth.currentUser?.uid != userId || token.isNullOrBlank()) {
            throw ApiException("Please sign in again.")
        }

        return "Bearer $token"
    }

    private suspend fun <T> body(
        request: suspend (String) -> Response<T>
    ): T {
        return execute(request).body()
            ?: throw ApiException("The API returned an empty response.")
    }

    private suspend fun <T> execute(
        request: suspend (String) -> Response<T>
    ): Response<T> {
        try {
            var response = request(authorization(forceRefresh = false))

            // A rejected token means the protected operation did not execute.
            if (response.code() == 401) {
                response.errorBody()?.close()
                response = request(authorization(forceRefresh = true))
            }

            if (FirebaseAuth.getInstance().currentUser?.uid != userId) {
                response.errorBody()?.close()
                throw ApiException("Your account changed. Please sign in again.")
            }

            Log.d("StudySync", "API response: ${response.code()}")

            if (!response.isSuccessful) {
                throw ApiException(responseMessage(response))
            }

            return response
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ApiException) {
            throw exception
        } catch (exception: JsonParseException) {
            throw ApiException("The API returned an unexpected response.")
        } catch (exception: IOException) {
            throw ApiException(
                "Connection failed. Check your internet and refresh the list " +
                        "before retrying a change."
            )
        }
    }

    private fun responseMessage(response: Response<*>): String {
        val fallback = when (response.code()) {
            401 -> "Your session has expired. Sign out and sign in again."
            403 -> "You do not have permission to perform this action."
            404 -> "This record is no longer available. Refresh the list."
            409 -> "This change conflicts with existing data."
            in 500..599 -> "The server is unavailable. Please try again shortly."
            else -> "The request failed (${response.code()})."
        }

        val raw = response.errorBody()?.string()

        // Show validation/conflict messages supplied by our API.
        if (response.code() in listOf(400, 404, 409)) {
            val message = runCatching {
                JSONObject(raw ?: "{}")
                    .getJSONObject("error")
                    .getString("message")
            }.getOrNull()

            if (!message.isNullOrBlank() && message.length <= 250) {
                return message
            }
        }

        return fallback
    }

    private fun Subject.toRequest() = SubjectRequest(
        name = name.trim(),
        lecturerName = lecturerName.trim()
    )

    private fun StudyTask.toRequest() = TaskRequest(
        subjectId = subjectId,
        title = title.trim(),
        description = description.trim(),
        dueDate = dueDate.trim(),
        priority = priority,
        completed = completed
    )
}