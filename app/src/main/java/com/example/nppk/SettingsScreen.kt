package com.example.nppk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme


@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val currentLogin = remember { mutableStateOf("") }
    val newLogin = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }

    val notificationsEnabled = rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background) // Явно задаем фон всего экрана
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Настройки",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Профиль",
            style = ScheduleTheme.typography.bodyMain,
            color = ScheduleTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Используем наш новый стилизованный компонент ввода
        SettingsTextField(
            value = currentLogin.value,
            onValueChange = { currentLogin.value = it },
            label = "Текущий логин",
            hint = "Введите логин"
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsTextField(
            value = newLogin.value,
            onValueChange = { newLogin.value = it },
            label = "Новый логин",
            hint = "Придумайте новый логин"
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsTextField(
            value = newPassword.value,
            onValueChange = { newPassword.value = it },
            label = "Новый пароль",
            hint = "Придумайте новый пароль",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Здесь позже будет безопасная логика смены логина и пароля.
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.accent
            )
        ) {
            Text(
                text = "Сохранить профиль",
                modifier = Modifier.padding(vertical = 4.dp),
                style = ScheduleTheme.typography.bodyMain,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.surfaceActive
            )
        ) {
            Text(
                text = "Выйти из аккаунта",
                modifier = Modifier.padding(vertical = 4.dp),
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider(color = ScheduleTheme.colors.surfaceActive)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Системные настройки",
            style = ScheduleTheme.typography.bodyMain,
            color = ScheduleTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            ) {
                Text(
                    text = "Уведомления",
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textPrimary
                )
                Text(
                    text = "Включить push-уведомления приложения",
                    style = ScheduleTheme.typography.bodySecondary,
                    color = ScheduleTheme.colors.textSecondary
                )
            }
            Switch(
                checked = notificationsEnabled.value,
                onCheckedChange = { notificationsEnabled.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ScheduleTheme.colors.accent,
                    checkedTrackColor = ScheduleTheme.colors.accent.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            ) {
                Text(
                    text = "Тёмная тема",
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textPrimary
                )
                Text(
                    text = "Использовать тёмное оформление интерфейса",
                    style = ScheduleTheme.typography.bodySecondary,
                    color = ScheduleTheme.colors.textSecondary
                )
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onDarkThemeChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ScheduleTheme.colors.accent,
                    checkedTrackColor = ScheduleTheme.colors.accent.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// Переиспользуемый компонент текстового поля для настроек
@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = ScheduleTheme.typography.bodySecondary,
            color = ScheduleTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = ScheduleTheme.typography.bodyMain,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = hint,
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textSecondary.copy(alpha = 0.5f) // Делаем текст полупрозрачным
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ScheduleTheme.colors.textPrimary,
                unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                cursorColor = ScheduleTheme.colors.accent,
                // Делаем рамки прозрачными
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                // Заливаем фон
                focusedContainerColor = ScheduleTheme.colors.surface,
                unfocusedContainerColor = ScheduleTheme.colors.surface
            )
        )
    }
}