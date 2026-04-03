package com.example.nppk.data.repository

import com.example.nppk.data.model.User
import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {

    // Пока возвращаем заглушку, но структура уже готова под реальное API
    override suspend fun getUserProfile(): User {
        delay(1000) // Имитация запроса к серверу
        return User(1, "ivan_ant", "Антипов Иван", "Студент", "400")
    }

    override suspend fun updateCredentials(newLogin: String, newPassword: String): Boolean {
        delay(1000)
        return true // Возвращаем успех
    }

    override suspend fun logout() {
        // Очистка токенов, базы данных и т.д.
    }
}