package com.example.schedule.feature.schedule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme

/**
 * Временный вариант: вместо крутилки и текста "Загрузка расписания..."
 * показываем простую статичную заглушку.
 */
@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Расписание пока недоступно",
            style = ScheduleTheme.typography.bodyMain,
            color = ScheduleTheme.colors.textSecondary
        )
    }
}