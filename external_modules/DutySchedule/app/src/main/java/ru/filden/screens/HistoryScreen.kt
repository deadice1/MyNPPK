package ru.filden.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch
import ru.filden.api.ApiClient
import ru.filden.api.DutyHistoryRecord

@Composable
fun HistoryScreen(
    apiClient: ApiClient,
    groupId: Int
) {
    var history by remember { mutableStateOf<List<DutyHistoryRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        scope.launch {
            isLoading = true
            history = apiClient.getDutyHistory(groupId)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "История дежурств",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ScheduleTheme.colors.accent)
            }
        } else if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("История дежурств пуста", color = ScheduleTheme.colors.textSecondary, style = ScheduleTheme.typography.bodyMain)
            }
        } else {
            LazyColumn {
                items(history) { record ->
                    HistoryCard(record = record)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryCard(record: DutyHistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScheduleTheme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Дата: ${record.date}",
                style = ScheduleTheme.typography.bodySecondary,
                color = ScheduleTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Дежурные: ${record.firstStudent.name}" +
                        (record.secondStudent?.let { ", $it" } ?: ""),
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.textPrimary
            )
        }
    }
}