package com.example.nppk

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nppk.ui.viewmodels.SettingsViewModel
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onOpenMyGroups: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    var isExpanded by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVerified by remember { mutableStateOf(false) }
    var isCurrentPasswordError by remember { mutableStateOf(false) }
    var isVerifyingCurrentPassword by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(currentPassword) {
        if (currentPassword.length >= 4) {
            isVerifyingCurrentPassword = true
            isCurrentPasswordError = false
            kotlinx.coroutines.delay(500)
            val isValid = viewModel.verifyCurrentPassword(currentPassword)
            isCurrentPasswordVerified = isValid
            isCurrentPasswordError = !isValid
            isVerifyingCurrentPassword = false
        } else {
            isCurrentPasswordVerified = false
            isCurrentPasswordError = false
            isVerifyingCurrentPassword = false
        }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(text = "Настройки", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.textPrimary)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showLogoutDialog = true }) {
                Icon(Icons.Outlined.ExitToApp, "Выйти", modifier = Modifier.size(32.dp), tint = ScheduleTheme.colors.error)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Выход", style = ScheduleTheme.typography.bodySecondary.copy(fontSize = 10.sp), color = ScheduleTheme.colors.error)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                    Text(user?.role?.displayName ?: "", style = ScheduleTheme.typography.h2, color = ScheduleTheme.colors.textPrimary)
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
                    SettingsField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = "Текущий пароль",
                        isPassword = true,
                        isVerifying = isVerifyingCurrentPassword,
                        isVerified = isCurrentPasswordVerified,
                        isError = isCurrentPasswordError
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Новый пароль",
                        isPassword = true,
                        enabled = isCurrentPasswordVerified
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isChangingPassword = true
                            viewModel.changePassword(currentPassword, newPassword) { success, error ->
                                isChangingPassword = false
                                if (success) {
                                    Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                                    isExpanded = false
                                    currentPassword = ""
                                    newPassword = ""
                                    isCurrentPasswordVerified = false
                                    isCurrentPasswordError = false
                                } else {
                                    Toast.makeText(context, error ?: "Ошибка при изменении пароля", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isCurrentPasswordVerified && newPassword.length >= 4 && !isChangingPassword,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScheduleTheme.colors.accent,
                            contentColor = Color.White,
                            disabledContainerColor = ScheduleTheme.colors.accent.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
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

        if (user?.role == com.example.nppk.data.model.UserRole.TEACHER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ScheduleTheme.colors.surface)
                    .clickable { onOpenMyGroups() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Мои группы", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = ScheduleTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text(text = "Системные настройки", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Тёмная тема", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
                Text("Использовать тёмное оформление", style = ScheduleTheme.typography.bodySecondary, color = ScheduleTheme.colors.textSecondary)
            }
            Switch(checked = isDarkTheme, onCheckedChange = { onDarkThemeChange(it) }, colors = SwitchDefaults.colors(checkedThumbColor = ScheduleTheme.colors.accent))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Уведомления", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
                Text("Получать уведомления о расписании", style = ScheduleTheme.typography.bodySecondary, color = ScheduleTheme.colors.textSecondary)
            }
            Switch(checked = isNotificationsEnabled, onCheckedChange = { onNotificationsEnabledChange(it) }, colors = SwitchDefaults.colors(checkedThumbColor = ScheduleTheme.colors.accent))
        }
    }
}

@Composable
fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    isVerifying: Boolean = false,
    isVerified: Boolean = false,
    isError: Boolean = false
) {
    val borderColor = when {
        isError -> ScheduleTheme.colors.error
        isVerified -> Color.Green
        else -> ScheduleTheme.colors.accent
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ScheduleTheme.colors.accent)
            } else if (isVerified) {
                Icon(Icons.Default.Check, contentDescription = "Верно", tint = Color.Green)
            } else if (isError) {
                Icon(Icons.Default.Close, contentDescription = "Неверно", tint = ScheduleTheme.colors.error)
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ScheduleTheme.colors.textPrimary,
            unfocusedTextColor = ScheduleTheme.colors.textPrimary,
            disabledTextColor = ScheduleTheme.colors.textPrimary.copy(alpha = 0.5f),
            focusedLabelColor = if (isError) ScheduleTheme.colors.error else ScheduleTheme.colors.accent,
            unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
            disabledLabelColor = ScheduleTheme.colors.textSecondary.copy(alpha = 0.5f),
            focusedBorderColor = borderColor,
            unfocusedBorderColor = if (isError) ScheduleTheme.colors.error else ScheduleTheme.colors.divider,
            disabledBorderColor = ScheduleTheme.colors.divider.copy(alpha = 0.5f),
            focusedContainerColor = ScheduleTheme.colors.background,
            unfocusedContainerColor = ScheduleTheme.colors.background,
            disabledContainerColor = ScheduleTheme.colors.background.copy(alpha = 0.5f)
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
