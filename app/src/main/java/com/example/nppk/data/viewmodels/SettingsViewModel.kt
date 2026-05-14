package com.example.nppk.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nppk.data.model.User
import com.example.nppk.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Состояние профиля: пока грузится - null
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _user.value = authRepository.getUserProfile()
            } catch (e: Exception) {
                // Обработка ошибки загрузки
            }
        }
    }

    suspend fun verifyCurrentPassword(password: String): Boolean {
        return authRepository.verifyPassword(password)
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            // Сначала проверяем текущий пароль
            if (!authRepository.verifyPassword(currentPassword)) {
                onResult(false, "Неверный текущий пароль")
                return@launch
            }

            val currentUser = _user.value ?: return@launch
            val success = authRepository.updateCredentials(currentUser.login, newPassword)
            if (success) {
                onResult(true, null)
            } else {
                onResult(false, "Ошибка при изменении пароля")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}