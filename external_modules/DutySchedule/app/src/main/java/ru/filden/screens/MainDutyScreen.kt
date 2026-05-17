package ru.filden.screens

import android.R
import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.filden.api.ApiClient
import ru.filden.api.DutyPair
import ru.filden.api.Student
import ru.filden.api.UserRole
import ru.filden.api.canConfirmDuty
import ru.filden.api.canSelectDuty
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDutyScreen(
    apiClient: ApiClient,
    groupId: Int,
    userRole: UserRole
) {
    val toolTipText = "Выбираются два студента из списка с наименьшим кол-вом дежурств,\n" +
            "или же случайно, в зависимости от выбора старосты(зам. старосты, зам.зам. старосты или другого отв. лица) или преподавателя, которые позже отмечают дежурство (дежурившие студенты должны отчитаться!)\n"+
            "Пример порядка дежурств:\n1+2\n3+4\n5+6\nИ так далее.. И по кругу.\nВ случае отсутствия студента, возможно выбрать другого, или может продежурить один.\n" +
            "В таком случае алгоритм рано или поздно позволит этому студенту нагнать остальных.\n" +
            "Если же студент пропустит дежурство несколько раз (и его кол-во дежуств окажется значительно меньше, чем у остальных)\n" +
            "он будет попадаться каждое второе дежурство, пока не нагонит остальных.\n"


    var currentDuty by remember { mutableStateOf<DutyPair?>(null) }
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true)}
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showToolTip by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        scope.launch {
            isLoading = true
            currentDuty = apiClient.getCurrentDuty(groupId)
            students = apiClient.getStudentsByGroup(groupId).filter { it.is_duty }
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

            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (userRole.canConfirmDuty()) {
            Button(
                onClick = {
                    scope.launch {
                        val success = completeDuty(currentDuty, apiClient)
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
            Button(
                onClick = {
                    currentDuty = getRandomPair(currentDuty?.copy(), students)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScheduleTheme.colors.textSecondary,
                    contentColor = ScheduleTheme.colors.background
                )) {
                Text("Случайная пара", style = ScheduleTheme.typography.bodyMain)
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
        Spacer(modifier = Modifier.height(110.dp))
        Column(
            Modifier.fillMaxSize()
                .background(ScheduleTheme.colors.background)

        ) {
            Box(contentAlignment = Alignment.CenterStart) {
                TextButton(onClick = { showToolTip = true }) {
                    Text(
                        "Как это работает?",
                        color = ScheduleTheme.colors.textSecondary
                    )
                }
            }
        }

    }


    if (showToolTip) {
        AlertDialog(
            onDismissRequest = { showToolTip = false },
            title = {Text("Как это работает?", color = ScheduleTheme.colors.textPrimary)},
            text = {Text(toolTipText,color = ScheduleTheme.colors.textPrimary)},
            containerColor = ScheduleTheme.colors.surface,
            confirmButton = {
                TextButton(onClick = { showToolTip = false }) {
                    Text("Понятно", color = ScheduleTheme.colors.accent)
                }
            }
        )
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
                            onStudentSelected?.invoke(Student(0, 0,"Не выбран", 0, 0, true))
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
suspend fun completeDuty(pair:DutyPair?, apiClient: ApiClient): Boolean{
        if(apiClient.incrementDutyCount(pair?.first?.id ?: 0)){
        if (pair?.second != null){
            apiClient.incrementDutyCount(pair.second!!.id)
         }
            if(!apiClient.saveDutyHistory(pair?.first?.id?:0, pair?.second?.id, pair?.first?.groupId?:0)) return false
            return true
        }
        return false
}
fun getRandomPair(pair: DutyPair?, students: List<Student>): DutyPair?{
    val randomPair: DutyPair? = pair
    var secondStudent: Student?
    val firstStudent: Student = students[Random.nextInt(0, students.size-1)]
    secondStudent = students[Random.nextInt(0, students.size-1)]
    if(firstStudent.id == secondStudent.id){
        secondStudent = students[Random.nextInt(0, students.size-1)]
    }
    randomPair?.first = firstStudent
    randomPair?.second = secondStudent
    return randomPair
}

