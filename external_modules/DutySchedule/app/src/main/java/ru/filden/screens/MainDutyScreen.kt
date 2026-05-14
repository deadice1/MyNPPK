package ru.filden.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch
import ru.filden.api.ApiClient
import ru.filden.api.DutyPair
import ru.filden.api.Student
import ru.filden.api.UserRole
import ru.filden.api.canConfirmDuty
import ru.filden.api.canSelectDuty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDutyScreen(
    apiClient: ApiClient,
    groupId: Int,
    userRole: UserRole
) {
    var currentDuty by remember { mutableStateOf<DutyPair?>(null) }
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true)}
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        scope.launch {
            isLoading = true
            currentDuty = apiClient.getCurrentDuty(groupId)
            students = apiClient.getStudentsByGroup(groupId)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Текущая пара дежурных",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ScheduleTheme.colors.accent)
            }
            return@Column
        }

        Spacer(modifier = Modifier.height(30.dp))

        StudentSelector(
            label = "Первый дежурный",
            selectedStudent = currentDuty?.first,
            students = students,
            readOnly = !userRole.canSelectDuty(),
            onStudentSelected = { student ->
                currentDuty = currentDuty?.copy(first = student)
                scope.launch {
                    apiClient.updateCurrentDuty(
                        groupId = groupId,
                        firstStudentId = student.id,
                        secondStudentId = currentDuty?.second?.id
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        StudentSelector(
            label = "Второй дежурный (опционально)",
            selectedStudent = currentDuty?.second,
            students = students,
            readOnly = !userRole.canSelectDuty(),
            onStudentSelected = { student ->
                currentDuty = currentDuty?.copy(second = student)
                scope.launch {
                    apiClient.updateCurrentDuty(
                        groupId = groupId,
                        firstStudentId = currentDuty?.first?.id ?: 0,
                        secondStudentId = student.id
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (userRole.canConfirmDuty()) {
            Button(
                onClick = {
                    scope.launch {
                        val success = apiClient.completeDuty(
                            groupId = groupId,
                            firstStudentId = currentDuty?.first?.id ?: 0,
                            secondStudentId = currentDuty?.second?.id
                        )
                        if (success) {
                            currentDuty = apiClient.getCurrentDuty(groupId)
                            students = apiClient.getStudentsByGroup(groupId)
                        } else {
                            showError = true
                            errorMessage = "Ошибка при завершении дежурства"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScheduleTheme.colors.accent,
                    contentColor = ScheduleTheme.colors.background
                )
            ) {
                Text("Отметить дежурство", style = ScheduleTheme.typography.bodyMain)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ScheduleTheme.colors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Только просмотр. Для подтверждения дежурства нужны права старосты, преподавателя или администратора.",
                    modifier = Modifier.padding(16.dp),
                    style = ScheduleTheme.typography.bodySecondary,
                    color = ScheduleTheme.colors.textPrimary
                )
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Ошибка", color = ScheduleTheme.colors.textPrimary) },
            text = { Text(errorMessage, color = ScheduleTheme.colors.textSecondary) },
            containerColor = ScheduleTheme.colors.surface,
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK", color = ScheduleTheme.colors.accent)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSelector(
    label: String,
    selectedStudent: Student?,
    students: List<Student>,
    readOnly: Boolean,
    onStudentSelected: ((Student) -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = ScheduleTheme.typography.bodySecondary,
            color = ScheduleTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded && !readOnly,
            onExpandedChange = { if (!readOnly) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStudent?.name ?: "Не выбран",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    if (!readOnly) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                textStyle = ScheduleTheme.typography.bodyMain,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !readOnly,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ScheduleTheme.colors.textPrimary,
                    unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                    disabledTextColor = ScheduleTheme.colors.textPrimary,
                    focusedLabelColor = ScheduleTheme.colors.accent,
                    unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
                    disabledLabelColor = ScheduleTheme.colors.textSecondary,
                    focusedBorderColor = ScheduleTheme.colors.accent,
                    unfocusedBorderColor = ScheduleTheme.colors.divider,
                    disabledBorderColor = ScheduleTheme.colors.divider,
                    focusedContainerColor = ScheduleTheme.colors.surface,
                    unfocusedContainerColor = ScheduleTheme.colors.surface,
                    disabledContainerColor = ScheduleTheme.colors.surface
                )
            )

            if (!readOnly) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(ScheduleTheme.colors.surface)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Не выбран",
                                style = ScheduleTheme.typography.bodyMain,
                                color = ScheduleTheme.colors.textPrimary
                            ) 
                        },
                        onClick = {
                            onStudentSelected?.invoke(Student(0, 0,"Не выбран", 0, 0))
                            expanded = false
                        }
                    )
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${student.name} (дежурств: ${student.countDuty})",
                                    style = ScheduleTheme.typography.bodyMain,
                                    color = ScheduleTheme.colors.textPrimary
                                )
                            },
                            onClick = {
                                onStudentSelected?.invoke(student)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}