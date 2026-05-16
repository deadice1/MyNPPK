
package ru.filden.api

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.sql.Date


class ApiClient(private val baseUrl: String) {

    private val client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            gson {
                setDateFormat("yyyy-MM-dd")
            }
        }
    }


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

    suspend fun getStudentById(userId: Int): ApiStudent?{
        return withContext(Dispatchers.IO){
            try {
                val response: ApiBaseResponse<ApiStudent> = client.get("$baseUrl/api/students/user/$userId").body()
                if (response.status == "success") response.data else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
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
                        groupId = apiStudent.groupId,
                        user_id = apiStudent.userId,
                        is_duty = apiStudent.isDuty

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
                        groupId = apiStudent.groupId,
                        user_id = apiStudent.userId,
                        is_duty = apiStudent.isDuty
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
                        groupId = apiStudent.groupId,
                        user_id = apiStudent.userId,
                        is_duty = apiStudent.isDuty
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



    @SuppressLint("SuspiciousIndentation")
    suspend fun getCurrentDuty(groupId: Int): DutyPair? {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiStudent>> =
                    client.get("$baseUrl/api/students/duty/current/$groupId").body()

                val students = response.data?.map { apiStudent ->
                    Student(
                        id = apiStudent.id,
                        name = apiStudent.name,
                        countDuty = apiStudent.countDuty,
                        groupId = apiStudent.groupId,
                        user_id = apiStudent.userId,
                        is_duty = apiStudent.isDuty
                    )
                } ?: emptyList()
                val current: DutyPair = DutyPair(students[0], students[1])
                    current
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

    suspend fun getDutyHistory(groupId: Int): List<DutyHistoryRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val response: ApiBaseResponse<List<ApiDutyHistory>> =
                    client.get("$baseUrl/api/duty-histories/group/$groupId").body()
                response.data?.map { history ->
                    val students = getStudentsByGroup(groupId)
                    val firstStudent = students.find { it.id == history.firstStudentId }
                        ?: Student(
                            id = history.firstStudentId,
                            name = getStudentById(history.firstStudentId)?.name ?: "Студент ${history.firstStudentId}",
                            countDuty = 0,
                            groupId = groupId,
                            user_id = history.firstStudentId,
                            is_duty = true
                        )
                    val secondStudent = history.secondStudentId?.let { studentId ->
                        students.find { it.id == studentId }
                            ?: Student(
                                id = studentId,
                                name = getStudentById(history.secondStudentId)?.name ?: "Студент $studentId",
                                countDuty = 0,
                                groupId = groupId,
                                user_id = history.secondStudentId,
                                is_duty = true
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
    suspend fun saveDutyHistory(firstStudentId:Int,secondStudentId:Int?,groupId:Int ): Boolean{
        return withContext(Dispatchers.IO) {
            try {
                val request = mutableMapOf(
                    "id" to 1,
                    "f_student_id" to firstStudentId,
                    "group" to groupId,
                    "date" to LocalDate.now().toString()
                ).apply {
                    if (secondStudentId != null && secondStudentId != 0) {
                        put("s_student_id", secondStudentId)
                    }
                }

                val response: ApiBaseResponse<*> = client.post("$baseUrl/api/duty-histories") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()

                println("Response: $response")
                response.status == "success"
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }




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


    suspend fun getAvailableGroupsForUser(userId: Int, userRole: UserRole): List<ApiGroup> {
        return when (userRole) {
            UserRole.ADMIN -> getAllGroups()
            UserRole.TEACHER -> {
                val allGroups = getAllGroups()
                val teacherGroups = getTeacherGroups(userId)
                allGroups.filter { teacherGroups.contains(it.id) }
            }
            else -> getAllGroups()
        }
    }

    fun close() {
        client.close()
    }
}