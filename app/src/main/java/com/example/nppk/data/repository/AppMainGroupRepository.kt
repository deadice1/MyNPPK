package com.example.nppk.data.repository

import com.example.schedule.shared.group.domain.repository.MainGroupRepository

class AppMainGroupRepository(private val authRepository: AuthRepository) : MainGroupRepository {
    override suspend fun getMainGroup(): String {
        val user = authRepository.getUserProfile()
        return if (user.groupNumber != "Нет группы") {
            user.groupNumber
        } else {
            // Если группа не установлена, возвращаем что-то по умолчанию или пустую строку
            ""
        }
    }
}
