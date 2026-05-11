package com.example.nppk

import android.util.Log
import androidx.compose.foundation.Image // Для Image
import androidx.compose.ui.res.painterResource // Если нужно
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nppk.ui.viewmodels.SettingsViewModel
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.androidx.compose.koinViewModel

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    var isExpanded by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val isCurrentPasswordValid = currentPassword.length >= 4
    var isChangingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    if (showLogoutDialog) {
        AppDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "Выход",
            text = "Вы уверены, что хотите выйти из аккаунта?",
            confirmText = "Выйти",
            dismissText = "Отмена",
            onConfirm = { 
                showLogoutDialog = false
                viewModel.logout()
                onLogout() 
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        // --- ЗАГОЛОВОК ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(text = "Настройки", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.textPrimary)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showLogoutDialog = true }) {
                Icon(Icons.Outlined.ExitToApp, "Выйти", modifier = Modifier.size(32.dp), tint = ScheduleTheme.colors.error)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Выход", style = ScheduleTheme.typography.bodySecondary.copy(fontSize = 10.sp), color = ScheduleTheme.colors.error)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- КАРТОЧКА ПРОФИЛЯ ---
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(ScheduleTheme.colors.surface).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(ScheduleTheme.colors.accent.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    val initials = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "??"
                    Text(initials, style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.accent)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user?.fullName ?: "Загрузка...", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.textPrimary)
                    Text(user?.role ?: "", style = ScheduleTheme.typography.h2, color = ScheduleTheme.colors.textPrimary)
                    Text("Группа: ${user?.groupNumber ?: "..."}", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textSecondary)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Развернуть",
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                    tint = ScheduleTheme.colors.textPrimary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsField(currentPassword, { currentPassword = it }, "Текущий пароль", isPassword = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsField(newPassword, { newPassword = it }, "Новый пароль", isPassword = true, enabled = isCurrentPasswordValid)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            isChangingPassword = true
                            viewModel.changePassword(newPassword) { success ->
                                isChangingPassword = false
                                if (success) {
                                    Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                                    isExpanded = false
                                    currentPassword = ""
                                    newPassword = ""
                                } else {
                                    Toast.makeText(context, "Ошибка при изменении пароля", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isCurrentPasswordValid && newPassword.length >= 4 && !isChangingPassword,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScheduleTheme.colors.accent)
                    ) {
                        if (isChangingPassword) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Сохранить пароль")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- СИСТЕМНЫЕ НАСТРОЙКИ ---
        Text(text = "Системные настройки", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Тёмная тема", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
                Text("Использовать тёмное оформление", style = ScheduleTheme.typography.bodySecondary, color = ScheduleTheme.colors.textSecondary)
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onDarkThemeChange(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = ScheduleTheme.colors.accent)
            )
        }
    }
}

@Composable
fun SettingsField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false, enabled: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScheduleTheme.colors.textPrimary,
            unfocusedTextColor = ScheduleTheme.colors.textPrimary,
            focusedLabelColor = ScheduleTheme.colors.accent,
            unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
            focusedBorderColor = ScheduleTheme.colors.accent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = ScheduleTheme.colors.background,
            unfocusedContainerColor = ScheduleTheme.colors.background,
            disabledContainerColor = ScheduleTheme.colors.surface
        )
    )
}

@Composable
fun AppDialog(onDismissRequest: () -> Unit, title: String, text: String, confirmText: String, dismissText: String, onConfirm: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ScheduleTheme.colors.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = ScheduleTheme.typography.h2, color = ScheduleTheme.colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = text, style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) { Text(dismissText, color = ScheduleTheme.colors.textSecondary) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ScheduleTheme.colors.error)) {
                    Text(confirmText, color = Color.White)
                }
            }
        }
    }
}