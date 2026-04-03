package com.example.nppk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Состояния для профиля
    var isExpanded by remember { mutableStateOf(false) }
    var currentLogin by remember { mutableStateOf("") }
    var newLogin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val isCurrentLoginValid = currentLogin.length >= 3 // Логика активации полей

    if (showLogoutDialog) {
        AppDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "Выход",
            text = "Вы уверены, что хотите выйти из аккаунта?",
            confirmText = "Выйти",
            dismissText = "Отмена",
            onConfirm = { showLogoutDialog = false; onLogout() }
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
        // --- ЗАГОЛОВОК И ВЫХОД ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Настройки", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.textPrimary)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showLogoutDialog = true }
            ) {
                Icon(Icons.Outlined.ExitToApp, "Выйти", modifier = Modifier.size(32.dp), tint = ScheduleTheme.colors.error)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Выход", style = ScheduleTheme.typography.bodySecondary.copy(fontSize = 10.sp), color = ScheduleTheme.colors.error)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- КАРТОЧКА ПРОФИЛЯ ---
        Text(text = "Профиль", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ScheduleTheme.colors.surface)
                .padding(20.dp)
        ) {
            // Верхняя часть с аватаром
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(ScheduleTheme.colors.accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("АИ", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.accent)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Антипов Иван", style = ScheduleTheme.typography.h1, color = ScheduleTheme.colors.textPrimary)
                    Text("Студент", style = ScheduleTheme.typography.h2, color = ScheduleTheme.colors.textPrimary)
                    Text("Группа: 400", style = ScheduleTheme.typography.bodyMain, color = ScheduleTheme.colors.textSecondary)
                }
                // Стрелка
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Развернуть",
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                    tint = ScheduleTheme.colors.textPrimary
                )
            }

            // Раскрывающаяся часть
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsField(currentLogin, { currentLogin = it }, "Текущий логин")
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsField(newLogin, { newLogin = it }, "Новый логин", enabled = isCurrentLoginValid)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsField(newPassword, { newPassword = it }, "Новый пароль", isPassword = true, enabled = isCurrentLoginValid)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Логика сохранения */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isCurrentLoginValid && (newLogin.isNotEmpty() || newPassword.isNotEmpty()),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScheduleTheme.colors.accent)
                    ) {
                        Text("Сохранить изменения")
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

// Диалог смены данных
@Composable
fun ChangeDataDialog(onDismissRequest: () -> Unit, onSave: (String, String) -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ScheduleTheme.colors.surface)
                .padding(24.dp)
        ) {
            Text("Смена данных", style = ScheduleTheme.typography.h2, color = ScheduleTheme.colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Функция-хелпер для стилизации полей
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ScheduleTheme.colors.textPrimary,
                unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                focusedLabelColor = ScheduleTheme.colors.accent,
                unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
                focusedBorderColor = ScheduleTheme.colors.accent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = ScheduleTheme.colors.background,
                unfocusedContainerColor = ScheduleTheme.colors.background
            )

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Новый логин") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors // Применяем наши цвета
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Новый пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors // Применяем наши цвета
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismissRequest) {
                    Text("Отмена", color = ScheduleTheme.colors.textSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(login, password) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScheduleTheme.colors.accent)
                ) {
                    Text("Сохранить", color = Color.White)
                }
            }
        }
    }
}

// Диалог подтверждения выхода
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

@Composable
fun SettingsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ScheduleTheme.colors.accent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = ScheduleTheme.colors.background,
            unfocusedContainerColor = ScheduleTheme.colors.background,
            disabledContainerColor = ScheduleTheme.colors.surface // Цвет, когда поле заблокировано
        )
    )
}