package com.example.nppk.data.repository

import android.content.Context
import com.example.nppk.data.api.AuthApi
import com.example.nppk.data.api.LoginRequest
import com.example.nppk.data.api.UpdateUserRequest
import com.example.nppk.data.model.User
import kotlinx.coroutines.delay

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("nppk_prefs", Context.MODE_PRIVATE)
    private var currentUser: User? = null

    init {
        val id = prefs.getInt("user_id", -1)
        if (id != -1) {
            currentUser = User(
                id = id,
                login = prefs.getString("user_login", "") ?: "",
                fullName = prefs.getString("user_fullname", "") ?: "",
                role = prefs.getString("user_role", "") ?: "",
                groupNumber = prefs.getString("user_group", "") ?: ""
            )
        }
    }

    override suspend fun getUserProfile(): User {
        if (currentUser == null) {
            val id = prefs.getInt("user_id", -1)
            if (id != -1) {
                currentUser = User(
                    id = id,
                    login = prefs.getString("user_login", "") ?: "",
                    fullName = prefs.getString("user_fullname", "") ?: "",
                    role = prefs.getString("user_role", "") ?: "",
                    groupNumber = prefs.getString("user_group", "") ?: ""
                )
            }
        }
        return currentUser ?: User(1, "guest", "Гость", "Студент", "Нет группы")
    }

    override suspend fun updateCredentials(newLogin: String, newPassword: String): Boolean {
        val user = currentUser ?: return false
        val roleId = if (user.role == "Преподаватель") 2 else 1
        return try {
            val request = UpdateUserRequest(login = newLogin, password = newPassword, role = roleId)
            val response = authApi.updateUser(user.id, request)
            response.status == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun login(login: String, password: String): Boolean {
        return try {
            val response = authApi.login(LoginRequest(login, password))
            if (response.status == "success" && response.data?.authenticated == true) {
                val apiUser = response.data.user ?: return false
                var fullName = "Неизвестный"
                var groupNumber = "Нет группы"
                
                val roleName = if (apiUser.role == 2) {
                    try {
                        val teacherResponse = authApi.getTeacherByUserId(apiUser.id)
                        if (teacherResponse.status == "success") {
                            fullName = teacherResponse.data?.name ?: "Неизвестный"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    "Преподаватель"
                } else {
                    try {
                        val studentsResponse = authApi.getStudents()
                        if (studentsResponse.status == "success") {
                            val student = studentsResponse.data?.find { it.user_id == apiUser.id }
                            if (student != null) {
                                fullName = student.name
                                val groupResponse = authApi.getGroupById(student.group_h)
                                if (groupResponse.status == "success") {
                                    groupNumber = groupResponse.data?.name ?: "Нет группы"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    "Студент"
                }
                
                currentUser = User(
                    id = apiUser.id,
                    login = apiUser.login,
                    fullName = fullName,
                    role = roleName,
                    groupNumber = groupNumber
                )
                
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putInt("user_id", apiUser.id)
                    .putString("user_login", apiUser.login)
                    .putString("user_fullname", fullName)
                    .putString("user_role", roleName)
                    .putString("user_group", groupNumber)
                    .commit()
                    
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    override fun getCachedRole(): String {
        return prefs.getString("user_role", "Студент") ?: "Студент"
    }

    override suspend fun logout() {
        currentUser = null
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_id")
            .remove("user_login")
            .remove("user_fullname")
            .remove("user_role")
            .remove("user_group")
            .commit()
    }
}