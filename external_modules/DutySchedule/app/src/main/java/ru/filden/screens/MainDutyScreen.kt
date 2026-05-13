package ru.filden.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Текущая пара дежурных",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                    .height(56.dp)
            ) {
                Text("Отметить дежурство")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Только просмотр. Для подтверждения дежурства нужны права старосты, преподавателя или администратора.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Ошибка") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showError = false }) {
                    Text("OK")
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
            style = MaterialTheme.typography.bodyMedium,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !readOnly,
                shape = RoundedCornerShape(12.dp)
            )

            if (!readOnly) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Не выбран") },
                        onClick = {
                            onStudentSelected?.invoke(Student(0, "Не выбран", 0, 0))
                            expanded = false
                        }
                    )
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = {
                                Text("${student.name} (дежурств: ${student.countDuty})")
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