package com.example.nppk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onLoginAsGuest: () -> Unit
) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Используем наш стилизованный компонент
        AuthTextField(
            value = loginState.value,
            onValueChange = { loginState.value = it },
            label = "Логин",
            hint = "Введите логин"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = "Пароль",
            hint = "••••••••",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Временно пропускаем любую введённую комбинацию логина и пароля.
                onLogin()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), // Делаем закругление как у полей ввода
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.accent
            )
        ) {
            Text(
                text = "Войти",
                modifier = Modifier.padding(vertical = 4.dp), // Делаем кнопку "пухлее"
                style = ScheduleTheme.typography.bodyMain,
                color = Color.White // Жестко белый цвет текста для акцентной кнопки
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onLoginAsGuest() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.surfaceActive
            )
        ) {
            Text(
                text = "Войти как гость",
                modifier = Modifier.padding(vertical = 4.dp),
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.textPrimary
            )
        }
    }
}

// Переиспользуемый компонент текстового поля для авторизации
@Composable
private fun AuthTextField(
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
                    color = ScheduleTheme.colors.textSecondary.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ScheduleTheme.colors.textPrimary,
                unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                cursorColor = ScheduleTheme.colors.accent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = ScheduleTheme.colors.surface,
                unfocusedContainerColor = ScheduleTheme.colors.surface
            )
        )
    }
}