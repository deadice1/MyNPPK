package com.example.nppk.data.repository

import com.example.nppk.data.model.User
import com.example.nppk.data.model.UserRole

interface AuthRepository {
    // Получить данные профиля (Иван, Студент, Группа 400)
    suspend fun getUserProfile(): User

    // Смена пароля/логина
    suspend fun updateCredentials(newLogin: String, newPassword: String): Boolean

    // Проверка текущего пароля
    suspend fun verifyPassword(password: String): Boolean

    // Авторизация
    suspend fun login(login: String, password: String): Boolean

    // Проверка авторизации
    fun isLoggedIn(): Boolean

    // Получить кэшированную роль пользователя
    fun getCachedRole(): UserRole

    // Выход из аккаунта
    suspend fun logout()

    // Проверка, является ли этот вход первым для преподавателя
    fun isTeacherFirstLogin(): Boolean

    // Отметить, что первый вход преподавателя завершен
    fun setTeacherFirstLoginCompleted()
}