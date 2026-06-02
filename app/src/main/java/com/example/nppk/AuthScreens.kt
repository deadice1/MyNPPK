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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nppk.ui.viewmodels.LoginViewModel
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onLoginAsGuest: () -> Unit,
    onTeacherFirstLogin: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error!!,
                color = ScheduleTheme.colors.error,
                style = ScheduleTheme.typography.bodySecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.login(
                    loginState.value, 
                    passwordState.value, 
                    onSuccess = onLogin,
                    onTeacherFirstLogin = onTeacherFirstLogin
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.accent,
                disabledContainerColor = ScheduleTheme.colors.surfaceActive
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = ScheduleTheme.colors.textPrimary,
                    modifier = Modifier.padding(vertical = 4.dp).height(24.dp)
                )
            } else {
                Text(
                    text = "Войти",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = ScheduleTheme.typography.bodyMain,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onLoginAsGuest() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.surfaceActive
            ),
            enabled = !isLoading
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