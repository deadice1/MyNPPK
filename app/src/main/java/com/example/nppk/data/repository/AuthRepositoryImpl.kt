package com.example.nppk.data.repository

import android.content.Context
import com.example.nppk.data.api.AuthApi
import com.example.nppk.data.api.LoginRequest
import com.example.nppk.data.api.UpdateUserRequest
import com.example.nppk.data.model.User
import com.example.nppk.data.model.UserRole
import com.example.nppk.util.HashUtils
import com.example.nppk.util.SecureStorage
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
                role = UserRole.fromId(prefs.getInt("user_role_id", UserRole.STUDENT.id)),
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
                    role = UserRole.fromId(prefs.getInt("user_role_id", UserRole.STUDENT.id)),
                    groupNumber = prefs.getString("user_group", "") ?: ""
                )
            }
        }
        return currentUser ?: User(1, "guest", "Гость", UserRole.STUDENT, "Нет группы")
    }

    override suspend fun updateCredentials(newLogin: String, newPassword: String): Boolean {
        val user = currentUser ?: return false
        return try {
            val hashedPassword = HashUtils.sha256(newPassword)
            val request = UpdateUserRequest(login = newLogin, password = hashedPassword, role = user.role.id)
            val response = authApi.updateUser(user.id, request)
            response.status == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun verifyPassword(password: String): Boolean {
        val user = currentUser ?: return false
        return try {
            val hashedPassword = HashUtils.sha256(password)
            val response = authApi.login(LoginRequest(user.login, hashedPassword))
            response.status == "success" && response.data?.authenticated == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun login(login: String, password: String): Boolean {
        return try {
            val hashedPassword = HashUtils.sha256(password)
            val response = authApi.login(LoginRequest(login, hashedPassword))
            if (response.status == "success" && response.data?.authenticated == true) {
                val apiUser = response.data.user ?: return false
                var fullName = "Неизвестный"
                var groupNumber = "Нет группы"
                val role = UserRole.fromId(apiUser.role)

                if (role == UserRole.TEACHER) {
                    try {
                        val teacherResponse = authApi.getTeacherByUserId(apiUser.id)
                        if (teacherResponse.status == "success") {
                            fullName = teacherResponse.data?.name ?: "Неизвестный"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
                }

                currentUser = User(
                    id = apiUser.id,
                    login = apiUser.login,
                    fullName = fullName,
                    role = role,
                    groupNumber = groupNumber
                )

                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putInt("user_id", apiUser.id)
                    .putString("user_login", apiUser.login)
                    .putString("user_fullname", fullName)
                    .putInt("user_role_id", role.id)
                    .putString("user_group", groupNumber)
                    .commit()

                SecureStorage.saveCredentials(context, login, password)

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

    override fun getCachedRole(): UserRole {
        return UserRole.fromId(prefs.getInt("user_role_id", UserRole.STUDENT.id))
    }

    override suspend fun logout() {
        currentUser = null
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_id")
            .remove("user_login")
            .remove("user_fullname")
            .remove("user_role_id")
            .remove("user_group")
            .remove("teacher_first_login_completed") // Сбрасываем при выходе
            .commit()
            
        // Мы НЕ очищаем SecureStorage здесь. 
        // Данные (зашифрованные) остаются для биометрического входа.
        // Если войдет другой пользователь, они просто перезапишутся.
    }

    override fun isTeacherFirstLogin(): Boolean {
        val role = getCachedRole()
        val completed = prefs.getBoolean("teacher_first_login_completed", false)
        return role == UserRole.TEACHER && !completed
    }

    override fun setTeacherFirstLoginCompleted() {
        prefs.edit().putBoolean("teacher_first_login_completed", true).apply()
    }
}