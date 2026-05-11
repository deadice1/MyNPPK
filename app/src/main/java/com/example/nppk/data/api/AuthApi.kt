package com.example.nppk.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class LoginRequest(val login: String, val password: String)

data class UpdateUserRequest(val login: String, val password: String, val role: Int)

data class UpdateUserResponse(
    val status: String,
    val data: ApiUser?
)

data class LoginResponse(
    val status: String,
    val data: LoginResponseData?
)

data class LoginResponseData(
    val authenticated: Boolean,
    val user: ApiUser?
)

data class ApiUser(
    val id: Int,
    val login: String,
    val role: Int
)

data class StudentsResponse(
    val status: String,
    val data: List<ApiStudent>?
)

data class ApiStudent(
    val id: Int,
    val user_id: Int,
    val name: String,
    val group_h: Int
)

data class TeacherResponse(
    val status: String,
    val data: ApiTeacher?
)

data class ApiTeacher(
    val id: Int,
    val user_id: Int,
    val name: String
)

data class SingleGroupResponse(
    val status: String,
    val data: ApiGroup?
)

data class ApiGroup(
    val id: Int,
    val name: String
)

interface AuthApi {
    @POST("api/users/authenticate")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/students")
    suspend fun getStudents(): StudentsResponse

    @GET("api/teachers/user/{userId}")
    suspend fun getTeacherByUserId(@Path("userId") userId: Int): TeacherResponse

    @GET("api/groups/{id}")
    suspend fun getGroupById(@Path("id") id: Int): SingleGroupResponse

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UpdateUserRequest): UpdateUserResponse
}
