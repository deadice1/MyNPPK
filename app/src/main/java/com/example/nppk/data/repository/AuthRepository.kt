package com.example.nppk.data.repository

import com.example.nppk.data.model.User

interface AuthRepository {
    // Получить данные профиля (Иван, Студент, Группа 400)
    suspend fun getUserProfile(): User

    // Смена пароля/логина
    suspend fun updateCredentials(newLogin: String, newPassword: String): Boolean

    // Авторизация
    suspend fun login(login: String, password: String): Boolean

    // Проверка авторизации
    fun isLoggedIn(): Boolean
    
    // Получить кэшированную роль пользователя
    fun getCachedRole(): String

    // Выход из аккаунта
    suspend fun logout()
}