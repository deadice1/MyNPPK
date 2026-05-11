package com.example.schedule.feature.schedule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.schedule.feature.schedule.presentation.ScheduleViewModel
import com.example.schedule.feature.schedule.presentation.TeacherScheduleViewModel
import com.example.schedule.feature.schedule.presentation.TeacherSettingsViewModel
import com.example.schedule.libs.navigation.Screen

// ВАЖНО: Используем правильный импорт для Compose
import org.koin.androidx.compose.koinViewModel

class MainTestScreen(private val isTeacherRole: Boolean = false) : Screen {

    @Composable
    override fun Render() {
        var isTeacherMode by remember { mutableStateOf(isTeacherRole) }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isTeacherMode) {
                TeacherContent()
            } else {
                StudentContent()
            }

            if (isTeacherRole) {
                Button(
                    onClick = { isTeacherMode = !isTeacherMode },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 50.dp)
                ) {
                    Text(text = if (isTeacherMode) "Вернуться к студенту" else "Режим учителя")
                }
            }
        }
    }

    @Composable
    private fun StudentContent() {
        // ПОЛУЧАЕМ VIEWMODEL ЗДЕСЬ: теперь она привязана к жизненному циклу Compose
        val studentViewModel: ScheduleViewModel = koinViewModel()

        val state by studentViewModel.state.collectAsState()

        LaunchedEffect(Unit) { studentViewModel.loadInitialData() }

        ScheduleContent(
            state = state,
            onSelectedScheduleIndexChangedListener = studentViewModel::updateSelectedScheduleIndex,
            onOpenGroupSelectorListener = studentViewModel::startGroupSelecting,
            onCloseGroupSelectorListener = studentViewModel::cancelGroupSelecting,
            onGroupSelectedListener = studentViewModel::selectNewGroup,
            onPreviousDayListener = studentViewModel::getPreviousDay,
            onNextDayListener = studentViewModel::getNextDay,
            onDateSelectedListener = studentViewModel::selectDate
        )
    }

    @Composable
    private fun TeacherContent() {
        // ПОЛУЧАЕМ VIEWMODELS ЗДЕСЬ
        val teacherViewModel: TeacherScheduleViewModel = koinViewModel()
        val teacherSettingsViewModel: TeacherSettingsViewModel = koinViewModel()

        val state by teacherViewModel.state.collectAsState()
        var showSettings by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { teacherViewModel.loadInitialData() }

        if (showSettings) {
            TeacherSettingsBottomSheet(
                viewModel = teacherSettingsViewModel,
                onDismiss = {
                    showSettings = false
                    teacherViewModel.reloadCurrentSchedule()
                }
            )
        }

        ScheduleContent(
            state = state,
            onSelectedScheduleIndexChangedListener = teacherViewModel::updateSelectedScheduleIndex,
            onOpenGroupSelectorListener = { },
            onCloseGroupSelectorListener = { },
            onGroupSelectedListener = { },
            onPreviousDayListener = teacherViewModel::getPreviousDay,
            onNextDayListener = teacherViewModel::getNextDay,
            onDateSelectedListener = teacherViewModel::selectDate,
            onSettingsClickListener = { showSettings = true },
        )
    }
}