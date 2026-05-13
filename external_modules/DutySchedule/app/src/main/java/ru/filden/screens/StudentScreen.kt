package ru.filden.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Список студентов (${students.size})",
                style = MaterialTheme.typography.headlineMedium
            )

            if (userRole.canManageStudents()) {
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (students.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("В группе нет студентов")
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
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дежурств: ${student.countDuty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (canEditCount) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onIncrementDuty,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Увеличить счетчик")
                        }
                    }
                }
            }

            if (canEdit) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }
}

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
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя студента") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (canEditCount) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = countDuty.toString(),
                        onValueChange = { countDuty = it.toIntOrNull() ?: 0 },
                        label = { Text("Количество дежурств") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, if (canEditCount) countDuty else initialCount)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
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
}