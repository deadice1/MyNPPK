package ru.filden.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiClient(private val baseUrl: String) {

    private val client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            gson {
                setDateFormat("yyyy-MM-dd HH:mm:ss")
            }
        }
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    // ========== Пользователи ==========

    suspend fun getUser(userId: Int): ApiUser? {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<ApiUser> = client.get("$baseUrl/api/users/$userId").body()
                if (response.status == "success") response.data else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getUserRole(userId: Int): UserRole? {
        val user = getUser(userId)
        return user?.let { UserRole.fromApiValue(it.role) }
    }

    // ========== Группы ==========

    suspend fun getAllGroups(): List<ApiGroup> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiGroup>> = client.get("$baseUrl/api/groups").body()
                response.data ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getGroup(groupId: Int): ApiGroup? {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<ApiGroup> = client.get("$baseUrl/api/groups/$groupId").body()
                if (response.status == "success") response.data else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // ========== Студенты ==========

    suspend fun getStudentsByGroup(groupId: Int): List<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiStudent>> =
                    client.get("$baseUrl/api/students/group/$groupId").body()
                response.data?.map { apiStudent ->
                    Student(
                        id = apiStudent.id,
                        name = apiStudent.name,
                        countDuty = apiStudent.countDuty,
                        groupId = apiStudent.groupId
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getAllStudents(): List<Student> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiStudent>> =
                    client.get("$baseUrl/api/students").body()
                response.data?.map { apiStudent ->
                    Student(
                        id = apiStudent.id,
                        name = apiStudent.name,
                        countDuty = apiStudent.countDuty,
                        groupId = apiStudent.groupId
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun createStudent(name: String, userId: Int, groupId: Int): Student? {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateStudentRequest(name, userId, groupId)
                val response: ApiBaseResponse<ApiStudent> = client.post("$baseUrl/api/students") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                response.data?.let { apiStudent ->
                    Student(
                        id = apiStudent.id,
                        name = apiStudent.name,
                        countDuty = apiStudent.countDuty,
                        groupId = apiStudent.groupId
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun updateStudent(studentId: Int, name: String, countDuty: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateStudentRequest(name, countDuty)
                val response: ApiBaseResponse<*> = client.put("$baseUrl/api/students/$studentId") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                response.status == "success"
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun incrementDutyCount(studentId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<*> =
                    client.patch("$baseUrl/api/students/$studentId/duty/increment").body()
                response.status == "success"
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun deleteStudent(studentId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<*> = client.delete("$baseUrl/api/students/$studentId").body()
                response.status == "success"
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // ========== Дежурства ==========

    suspend fun getCurrentDuty(groupId: Int): DutyPair? {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<ApiCurrentDuty> =
                    client.get("$baseUrl/api/duty/current/$groupId").body()
                if (response.status == "success" && response.data != null) {
                    val first = Student(
                        id = response.data.firstStudentId,
                        name = response.data.firstStudentName,
                        countDuty = 0,
                        groupId = groupId
                    )
                    val second = response.data.secondStudentId?.let { studentId ->
                        Student(
                            id = studentId,
                            name = response.data.secondStudentName ?: "",
                            countDuty = 0,
                            groupId = groupId
                        )
                    }
                    DutyPair(first, second)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun updateCurrentDuty(groupId: Int, firstStudentId: Int, secondStudentId: Int?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf(
                    "group_id" to groupId,
                    "first_student_id" to firstStudentId,
                    "second_student_id" to secondStudentId
                )
                val response: ApiBaseResponse<*> = client.put("$baseUrl/api/duty/current") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                response.status == "success"
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun completeDuty(groupId: Int, firstStudentId: Int, secondStudentId: Int?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = CompleteDutyRequest(firstStudentId, secondStudentId, groupId)
                val response: ApiBaseResponse<*> = client.post("$baseUrl/api/duty/complete") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
                response.status == "success"
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // ========== История дежурств ==========

    suspend fun getDutyHistory(groupId: Int): List<DutyHistoryRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiDutyHistory>> =
                    client.get("$baseUrl/api/duty-histories/group/$groupId").body()
                response.data?.map { history ->
                    val firstStudent = Student(
                        id = history.firstStudentId,
                        name = history.firstStudentName ?: "Студент ${history.firstStudentId}",
                        countDuty = 0,
                        groupId = groupId
                    )
                    val secondStudent = history.secondStudentId?.let { studentId ->
                        Student(
                            id = studentId,
                            name = history.secondStudentName ?: "Студент $studentId",
                            countDuty = 0,
                            groupId = groupId
                        )
                    }
                    DutyHistoryRecord(
                        id = history.id,
                        firstStudent = firstStudent,
                        secondStudent = secondStudent,
                        date = history.date
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // ========== Преподаватели и группы ==========

    suspend fun getTeacherGroups(teacherId: Int): List<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiTeacherGroup>> =
                    client.get("$baseUrl/api/teacher-groups/teacher/$teacherId").body()
                response.data?.map { it.groupId } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // ========== Вспомогательные методы ==========

    suspend fun getAvailableGroupsForUser(userId: Int, userRole: UserRole): List<ApiGroup> {
        return when (userRole) {
            UserRole.ADMIN -> getAllGroups()
            UserRole.TEACHER -> {
                val groups = getAllGroups()
                val teacherGroups = getTeacherGroups(userId)
                groups.filter { teacherGroups.contains(it.id) }
            }
            else -> getAllGroups()
        }
    }

    fun close() {
        client.close()
    }
}