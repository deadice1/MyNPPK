package com.example.nppk

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.nppk.ui.viewmodels.LoginViewModel
import com.example.nppk.util.BiometricHelper
import com.example.nppk.util.SecureStorage
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onLoginAsGuest: () -> Unit,
    onTeacherFirstLogin: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showBiometricButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedLogin = SecureStorage.getSavedLogin(context)
        val savedPass = SecureStorage.getSavedPassword(context)
        if (savedLogin != null && savedPass != null && BiometricHelper.isBiometricAvailable(context)) {
            showBiometricButton = true
        }
    }

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
            modifier = Modifier.fillMaxWidth().height(48.dp),
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
                    modifier = Modifier.padding(vertical = 4.dp).size(24.dp)
                )
            } else {
                Text(
                    text = "Войти",
                    style = ScheduleTheme.typography.bodyMain,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onLoginAsGuest() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.surfaceActive
            ),
            enabled = !isLoading
        ) {
            Text(
                text = "Войти как гость",
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.textPrimary
            )
        }

        if (showBiometricButton && activity != null) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        BiometricHelper.promptBiometricAuth(
                            activity = activity,
                            title = "Вход в приложение",
                            subtitle = "Приложите палец для входа",
                            onSuccess = {
                                val savedLogin = SecureStorage.getSavedLogin(context)
                                val savedPass = SecureStorage.getSavedPassword(context)
                                if (savedLogin != null && savedPass != null) {
                                    viewModel.login(savedLogin, savedPass, onLogin, onTeacherFirstLogin)
                                }
                            },
                            onFailed = {
                                Toast.makeText(context, "Отпечаток не распознан", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Ошибка: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ScheduleTheme.colors.surfaceActive,
                        contentColor = ScheduleTheme.colors.accent
                    )
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = "Вход по отпечатку", modifier = Modifier.size(36.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Войти по отпечатку",
                    style = ScheduleTheme.typography.bodySecondary,
                    color = ScheduleTheme.colors.textSecondary
                )
            }
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