package ru.filden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import com.example.schedule.shared.ui.ui.theme.typography
import ru.filden.logic.Schedule
import ru.filden.logic.ScheduleController
import ru.filden.logic.Student
import ru.filden.logic.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScheduleTheme {
                val controller = remember { ScheduleController() }
                val currentGroup = intent.getStringExtra("currentGroup") ?: "1"
                val userRole = intent.getStringExtra("userRole")?.let {
                    UserRole.valueOf(it)
                } ?: UserRole.STUDENT

                MainApp(controller, currentGroup, userRole)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    controller: ScheduleController,
    initialGroup: String,
    userRole: UserRole
) {
    val navController = rememberNavController()
    var currentGroup by remember { mutableStateOf(initialGroup) }
    val context = LocalContext.current

    Scaffold(
        containerColor = ScheduleTheme.colors.background,
        topBar = {
            MainTopBar(
                userRole = userRole,
                currentGroup = currentGroup,
                onGroupChange = { newGroup -> currentGroup = newGroup },
                canChangeGroup = userRole == UserRole.TEACHER,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScheduleTheme.colors.background)
                .padding(paddingValues)
        ) {
            NavBar(navController, userRole)

            NavHost(
                navController = navController,
                startDestination = NavRoutes.MainPage.route
            ) {
                composable(NavRoutes.MainPage.route) {
                    MainPage(
                        controller = controller,
                        groupId = currentGroup,
                        userRole = userRole
                    )
                }
                composable(NavRoutes.Students.route) {
                    StudentsPage(
                        controller = controller,
                        groupId = currentGroup,
                        userRole = userRole
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    userRole: UserRole,
    currentGroup: String,
    onGroupChange: (String) -> Unit,
    canChangeGroup: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val controller = remember { ScheduleController() }
    val groups = remember { controller.getAllGroups() }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // СТРЕЛКУ НАЗАД ПОЛНОСТЬЮ УДАЛИЛИ ОТСЮДА

                if (canChangeGroup) {
                    Box(
                        modifier = Modifier
                            // Убрали отступ слева, так как стрелки больше нет
                            .weight(1f)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            Text(
                                text = "Группа $currentGroup ▼",
                                modifier = Modifier
                                    .menuAnchor()
                                    .clickable { expanded = !expanded }
                                    .padding(vertical = 8.dp) // Чуть поправили отступ
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(ScheduleTheme.colors.surface)
                            ) {
                                groups.forEach { groupId ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Группа $groupId",
                                                color = ScheduleTheme.colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            onGroupChange(groupId)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Группа $currentGroup",
                        style = typography.h3,
                        color = ScheduleTheme.colors.textPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ScheduleTheme.colors.background,
            titleContentColor = ScheduleTheme.colors.textPrimary
        )
    )
}

@Composable
fun NavBar(navController: NavController, userRole: UserRole) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScheduleTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavButton(
            text = "Дежурства",
            onClick = { navController.navigate(NavRoutes.MainPage.route) }
        )
        NavButton(
            text = "Студенты",
            onClick = { navController.navigate(NavRoutes.Students.route) }
        )
    }
}

@Composable
fun NavButton(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = ScheduleTheme.colors.surfaceActive
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = typography.bodyMain,
            color = ScheduleTheme.colors.textPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    controller: ScheduleController,
    groupId: String,
    userRole: UserRole
) {
    val schedule = controller.getSchedule(groupId)
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (schedule == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Расписание для группы $groupId не найдено")
        }
        return
    }
    var currentPair by remember { mutableStateOf(schedule.currentPair) }
    val students = remember { mutableStateListOf<Student>().apply { addAll(schedule.students) } }

    LaunchedEffect(schedule.students.hashCode()) {
        students.clear()
        students.addAll(schedule.students)
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
            style = typography.h1,
            color = ScheduleTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
        StudentSelector(
            label = "Первый дежурный",
            selectedStudent = currentPair.first,
            students = students,
            onStudentSelected = if (userRole.canSelectDuty()) {
                { selected ->
                    currentPair = Schedule.Pair(selected, currentPair.second)
                }
            } else null,
            readOnly = !userRole.canSelectDuty()
        )

        Spacer(modifier = Modifier.height(20.dp))

        StudentSelector(
            label = "Второй дежурный",
            selectedStudent = currentPair.second,
            students = students,
            onStudentSelected = if (userRole.canSelectDuty()) {
                { selected ->
                    currentPair = Schedule.Pair(currentPair.first, selected)
                }
            } else null,
            readOnly = !userRole.canSelectDuty()
        )

        Spacer(modifier = Modifier.height(40.dp))
        if (userRole.canConfirmDuty()) {
            Button(
                onClick = {
                    try {
                        controller.completeDutyInGroup(groupId, currentPair)
                        controller.getSchedule(groupId)?.let { updatedSchedule ->
                            currentPair = updatedSchedule.currentPair
                            students.clear()
                            students.addAll(updatedSchedule.students)
                        }
                    } catch (e: Exception) {
                        showError = true
                        errorMessage = e.message ?: "Ошибка при завершении дежурства"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Продежурили",
                    style = typography.bodyMain,
                    color = ScheduleTheme.colors.surface
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = ScheduleTheme.colors.surface
                )
            ) {
                Text(
                    text = "Только просмотр. Для подтверждения дежурства нужны права старосты или преподавателя.",
                    modifier = Modifier.padding(16.dp),
                    style = typography.bodySecondary,
                    color = ScheduleTheme.colors.textSecondary
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
    selectedStudent: Student,
    students: List<Student>,
    onStudentSelected: ((Student) -> Unit)?,
    readOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Выносим текст над полем, чтобы избежать "чёрных квадратов" на рамке
        Text(
            text = label,
            style = typography.bodySecondary,
            color = ScheduleTheme.colors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded && !readOnly,
            onExpandedChange = { if (!readOnly) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStudent.name,
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
                shape = RoundedCornerShape(12.dp), // Делаем закругления как у карточек расписания
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ScheduleTheme.colors.textPrimary,
                    unfocusedTextColor = ScheduleTheme.colors.textPrimary,
                    disabledTextColor = ScheduleTheme.colors.textSecondary,
                    // Делаем рамки прозрачными, чтобы поле выглядело как сплошная карточка
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    // Заливаем фон цветом карточек
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
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${student.name} (дежурств: ${student.countDuty})",
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

@Composable
fun StudentsPage(
    controller: ScheduleController,
    groupId: String,
    userRole: UserRole
) {
    val students = remember { mutableStateListOf<Student>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var nextId by remember { mutableIntStateOf(100) }

    LaunchedEffect(groupId) {
        students.clear()
        students.addAll(controller.getStudents(groupId))
        nextId = (students.maxOfOrNull { it.uuid } ?: 0) + 1
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
                style = typography.h2,
                color = ScheduleTheme.colors.textPrimary
            )

            if (userRole.canManageStudents()) {
                Button(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                    Text("Добавить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(students) { student ->
                StudentItem(
                    student = student,
                    canEdit = userRole.canManageStudents(),
                    onEdit = {
                        if (userRole.canManageStudents()) {
                            selectedStudent = student
                            showEditDialog = true
                        }
                    },
                    onDelete = {
                        if (userRole.canManageStudents()) {
                            if (controller.removeStudentGlobally(student.uuid)) {
                                students.remove(student)
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog && userRole.canManageStudents()) {
        StudentDialog(
            title = "Добавить студента",
            student = Student("", nextId, 0, groupId),
            onConfirm = { newStudent ->
                controller.addStudentToGroup(newStudent, groupId)
                students.clear()
                students.addAll(controller.getStudents(groupId))
                nextId++
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog && selectedStudent != null && userRole.canManageStudents()) {
        StudentDialog(
            title = "Редактировать студента",
            student = selectedStudent!!.copy(),
            onConfirm = { updatedStudent ->
                if (controller.updateStudentInGroup(updatedStudent, groupId)) {
                    val index = students.indexOfFirst { it.uuid == updatedStudent.uuid }
                    if (index != -1) {
                        students[index] = updatedStudent
                    }
                }
                showEditDialog = false
                selectedStudent = null
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
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
            Column {
                Text(
                    text = student.name,
                    style = typography.h3,
                    color = ScheduleTheme.colors.textPrimary
                )
                Text(
                    text = "Дежурств: ${student.countDuty} | ID: ${student.uuid}",
                    style = typography.bodySecondary,
                    color = ScheduleTheme.colors.textSecondary
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDialog(
    title: String,
    student: Student,
    onConfirm: (Student) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(student.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = typography.h2,
                color = ScheduleTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя студента") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "ID: ${student.uuid} (генерируется автоматически)",
                    style = typography.bodyTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(student.copy(name = name))
                    }
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

fun UserRole.canSelectDuty(): Boolean = this == UserRole.HEADMAN || this == UserRole.TEACHER
fun UserRole.canConfirmDuty(): Boolean = this == UserRole.HEADMAN || this == UserRole.TEACHER
fun UserRole.canManageStudents(): Boolean = this == UserRole.HEADMAN || this == UserRole.TEACHER
fun UserRole.canChangeGroup(): Boolean = this == UserRole.TEACHER

sealed class NavRoutes(val route: String) {
    object MainPage : NavRoutes("mainPage")
    object Students : NavRoutes("students")
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectGroup() {

}