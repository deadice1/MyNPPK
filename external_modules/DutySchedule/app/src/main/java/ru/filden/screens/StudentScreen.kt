package ru.filden.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch
import ru.filden.api.ApiClient
import ru.filden.api.Student
import ru.filden.api.UserRole
import ru.filden.api.canEditStudentData
import ru.filden.api.canManageStudents

@Composable
fun StudentsScreen(
    apiClient: ApiClient,
    groupId: Int,
    userRole: UserRole
) {
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    val scope = rememberCoroutineScope()

    fun loadStudents() {
        scope.launch {
            isLoading = true
            students = apiClient.getStudentsByGroup(groupId)
            isLoading = false
        }
    }

    LaunchedEffect(groupId) {
        loadStudents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Список студентов (${students.size})",
                style = ScheduleTheme.typography.h1,
                color = ScheduleTheme.colors.textPrimary
            )
                /*
            if (userRole.canManageStudents()) {
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScheduleTheme.colors.accent,
                        contentColor = ScheduleTheme.colors.background
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить", style = ScheduleTheme.typography.bodyMain)
                }
            }
        */
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ScheduleTheme.colors.accent)
            }
        } else if (students.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "В группе нет студентов",
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textSecondary
                )
            }
        } else {
            LazyColumn {
                items(students) { student ->
                    StudentItem(
                        student = student,
                        canEdit = userRole.canManageStudents(),
                        canEditCount = userRole.canManageStudents(),
                        onEdit = {
                            selectedStudent = student
                            showEditDialog = true
                        },
                        onIncrementDuty = {
                            scope.launch {
                                if (apiClient.incrementDutyCount(student.id)) {
                                    loadStudents()
                                }
                            }
                        },
                        onDelete = {
                            if (userRole.canManageStudents()) {
                                scope.launch {
                                    if (apiClient.deleteStudent(student.id)) {
                                        loadStudents()
                                    }
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
/*
    if (showAddDialog) {
        StudentDialog(
            title = "Добавить студента",
            initialName = "",
            onConfirm = { name ->
                scope.launch {
                    apiClient.createStudent(name, userId = 0, groupId = groupId)
                    loadStudents()
                    showAddDialog = false
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog && selectedStudent != null) {
        StudentDialog(
            title = "Редактировать студента",
            initialName = selectedStudent!!.name,
            initialCount = selectedStudent!!.countDuty,
            canEditCount = userRole.canEditStudentData(),
            onConfirm = { name, countDuty ->
                scope.launch {
                    val countToUpdate = if (userRole.canEditStudentData()) countDuty else selectedStudent!!.countDuty
                    apiClient.updateStudent(selectedStudent!!.id, name, countToUpdate)
                    loadStudents()
                    showEditDialog = false
                    selectedStudent = null
                }
            },
            onDismiss = {
                showEditDialog = false
                selectedStudent = null
            }
        )
    }
}
*/
@Composable
fun StudentItem(
    student: Student,
    canEdit: Boolean,
    canEditCount: Boolean,
    onEdit: () -> Unit,
    onIncrementDuty: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScheduleTheme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name,
                    style = ScheduleTheme.typography.h2,
                    color = ScheduleTheme.colors.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дежурств: ${student.countDuty}",
                        style = ScheduleTheme.typography.bodyMain,
                        color = ScheduleTheme.colors.textSecondary
                    )
                    if (false) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onIncrementDuty,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Увеличить счетчик", tint = ScheduleTheme.colors.accent)
                        }
                    }
                }
            }

            if (false) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = ScheduleTheme.colors.accent)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = ScheduleTheme.colors.error)
                    }
                }
            }
        }
    }
}
/*
@Composable
fun StudentDialog(
    title: String,
    initialName: String,
    initialCount: Int = 0,
    canEditCount: Boolean = false,
    onConfirm: (name: String, countDuty: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var countDuty by remember { mutableStateOf(initialCount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ScheduleTheme.colors.surface,
        title = { Text(title, color = ScheduleTheme.colors.textPrimary, style = ScheduleTheme.typography.h2) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя студента") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = ScheduleTheme.typography.bodyMain,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ScheduleTheme.colors.textPrimary,
                        unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                        focusedLabelColor = ScheduleTheme.colors.accent,
                        unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
                        focusedBorderColor = ScheduleTheme.colors.accent,
                        unfocusedBorderColor = ScheduleTheme.colors.divider,
                        focusedContainerColor = ScheduleTheme.colors.surface,
                        unfocusedContainerColor = ScheduleTheme.colors.surface
                    )
                )

                if (canEditCount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = countDuty.toString(),
                        onValueChange = { countDuty = it.toIntOrNull() ?: 0 },
                        label = { Text("Количество дежурств") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = ScheduleTheme.typography.bodyMain,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ScheduleTheme.colors.textPrimary,
                            unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                            focusedLabelColor = ScheduleTheme.colors.accent,
                            unfocusedLabelColor = ScheduleTheme.colors.textSecondary,
                            focusedBorderColor = ScheduleTheme.colors.accent,
                            unfocusedBorderColor = ScheduleTheme.colors.divider,
                            focusedContainerColor = ScheduleTheme.colors.surface,
                            unfocusedContainerColor = ScheduleTheme.colors.surface
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, if (canEditCount) countDuty else initialCount)
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScheduleTheme.colors.accent,
                    contentColor = ScheduleTheme.colors.background
                )
            ) {
                Text("Сохранить", style = ScheduleTheme.typography.bodyMain)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ScheduleTheme.colors.textSecondary, style = ScheduleTheme.typography.bodyMain)
            }
        }
    )
}

@Composable
fun StudentDialog(
    title: String,
    initialName: String,
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    StudentDialog(
        title = title,
        initialName = initialName,
        initialCount = 0,
        canEditCount = false,
        onConfirm = { name, _ -> onConfirm(name) },
        onDismiss = onDismiss
    )
}*/