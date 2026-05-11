package com.example.nppk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nppk.ui.viewmodels.MyGroupsViewModel
import com.example.schedule.shared.group.domain.entity.Group
import com.example.schedule.shared.schedule.domain.entity.Subgroup
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.androidx.compose.koinViewModel

// Цвета из нового файла для логики предметов
private val ChipsSelect = Color(0xFF849CD0)
private val SurfaceLight = Color(0xFFF4F5F7)
private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF212121)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupsScreen(
    onBack: () -> Unit,
    viewModel: MyGroupsViewModel = koinViewModel()
) {
    val selectedGroups by viewModel.selectedGroups.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val tempSelectedGroups by viewModel.tempSelectedGroups.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val groupSubjectsMap by viewModel.groupSubjects.collectAsState()
    val drafts by viewModel.drafts.collectAsState()
    
    var isAddingGroups by remember { mutableStateOf(false) }

    // Состояние для управления развернутыми предметами групп (используем Map для железной стабильности)
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    if (isAddingGroups) {
        AddGroupsDialog(
            allGroups = groups,
            currentSelected = selectedGroups,
            onDismiss = { isAddingGroups = false },
            onGroupsAdded = { added ->
                viewModel.addGroups(added)
                isAddingGroups = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Мои группы",
                        style = ScheduleTheme.typography.h2,
                        color = ScheduleTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            viewModel.toggleSelectionMode(false)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = ScheduleTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.deleteSelectedGroups() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = ScheduleTheme.colors.error
                            )
                        }
                    } else {
                        IconButton(onClick = { isAddingGroups = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить",
                                tint = ScheduleTheme.colors.textPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ScheduleTheme.colors.background
                )
            )
        },
        containerColor = ScheduleTheme.colors.background
    ) { paddingValues ->
        if (selectedGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Вы не выбрали ни одной группы",
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                selectedGroups.forEach { groupName ->
                    val isSelectedForDeletion = tempSelectedGroups.contains(groupName)
                    val isExpanded = expandedStates[groupName] ?: false

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Карточка группы
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelectedForDeletion) ScheduleTheme.colors.chipsSelect 
                                    else ScheduleTheme.colors.surface
                                )
                                .pointerInput(groupName) {
                                    detectTapGestures(
                                        onLongPress = {
                                            viewModel.toggleSelectionMode(true)
                                            viewModel.toggleGroupForDeletion(groupName)
                                        },
                                        onTap = {
                                            if (isSelectionMode) {
                                                viewModel.toggleGroupForDeletion(groupName)
                                            } else {
                                                // ТОГГЛ СОСТОЯНИЯ
                                                // Берем самое свежее значение из мапы:
                                                val currentState = expandedStates[groupName] ?: false
                                                val nextState = !currentState

                                                expandedStates[groupName] = nextState
                                                if (nextState) {
                                                    viewModel.loadSubjectsIfNeed(groupName)
                                                }
                                            }
                                        }
                                    )
                                }
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Группа $groupName",
                                    style = ScheduleTheme.typography.h3,
                                    color = ScheduleTheme.colors.textPrimary
                                )
                                if (!isSelectionMode) {
                                    Icon(
                                        painter = painterResource(
                                            if (isExpanded) ScheduleTheme.colors.imageArrowUp 
                                            else ScheduleTheme.colors.imageArrowDown
                                        ),
                                        contentDescription = null,
                                        tint = ScheduleTheme.colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Список предметов
                        AnimatedVisibility(visible = isExpanded && !isSelectionMode) {
                            val subjects = groupSubjectsMap[groupName] ?: emptyList()
                            val groupDrafts = drafts.filter { it.groupName == groupName }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (subjects.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = ScheduleTheme.colors.accent
                                        )
                                    }
                                } else {
                                    subjects.forEach { subject ->
                                        key(groupName + subject) {
                                            val draft = groupDrafts.find { it.subjectName == subject }
                                            SubjectItem(
                                                subjectName = subject,
                                                isChecked = draft != null,
                                                selectedSubgroup = draft?.subgroup ?: Subgroup.ALL,
                                                onCheckChange = { isChecked ->
                                                    viewModel.toggleSubjectSelection(groupName, subject, isChecked)
                                                },
                                                onSubgroupChange = { subgroup ->
                                                    viewModel.updateSubgroup(groupName, subject, subgroup)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectItem(
    subjectName: String,
    isChecked: Boolean,
    selectedSubgroup: Subgroup,
    onCheckChange: (Boolean) -> Unit,
    onSubgroupChange: (Subgroup) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isChecked) ChipsSelect else SurfaceLight)
                .clickable { onCheckChange(!isChecked) }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .defaultMinSize(minHeight = 44.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isChecked) White else Black)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = subjectName,
                color = if (isChecked) White else Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        val showSubgroups = isChecked && isSubjectWithSubgroups(subjectName)

        AnimatedVisibility(visible = showSubgroups) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubgroupOption("Все", Subgroup.ALL, selectedSubgroup, onSubgroupChange)
                SubgroupOption("1 (а)", Subgroup.FIRST, selectedSubgroup, onSubgroupChange)
                SubgroupOption("2 (н)", Subgroup.SECOND, selectedSubgroup, onSubgroupChange)
            }
        }
    }
}

@Composable
private fun SubgroupOption(
    title: String,
    value: Subgroup,
    selectedValue: Subgroup,
    onClick: (Subgroup) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick(value) }) {
        RadioButton(
            selected = selectedValue == value,
            onClick = { onClick(value) },
            colors = RadioButtonDefaults.colors(selectedColor = ScheduleTheme.colors.accent)
        )
        Text(
            text = title,
            style = ScheduleTheme.typography.bodyTertiary,
            color = ScheduleTheme.colors.textPrimary,
            fontSize = 12.sp
        )
    }
}

private fun isSubjectWithSubgroups(subjectName: String): Boolean {
    val nameLower = subjectName.lowercase()
    return nameLower.contains("ин.язык") ||
            nameLower.contains("индив.проект") ||
            nameLower.contains("живопись с осн.цвет") ||
            nameLower.contains("рисунок с осн.персп")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupsDialog(
    allGroups: List<Group>,
    currentSelected: List<String>,
    onDismiss: () -> Unit,
    onGroupsAdded: (List<String>) -> Unit
) {
    var selectedInDialog by remember { mutableStateOf(emptySet<String>()) }
    val expandedCourses = remember { mutableStateListOf<Int>() }

    val groupsByCourse = remember(allGroups, currentSelected) {
        allGroups.filter { it.name !in currentSelected }
            .groupBy { group -> group.name.firstOrNull()?.digitToIntOrNull() ?: 0 }
            .toSortedMap()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = ScheduleTheme.colors.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                    Text(
                        "Добавить группу",
                        style = ScheduleTheme.typography.h2,
                        color = ScheduleTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupsByCourse.forEach { (course, courseGroups) ->
                        val isExpanded = expandedCourses.contains(course)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isExpanded) expandedCourses.remove(course)
                                        else expandedCourses.add(course)
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (course > 0) "$course курс" else "Другие",
                                    style = ScheduleTheme.typography.h4,
                                    color = ScheduleTheme.colors.textSecondary
                                )
                                Icon(
                                    painter = painterResource(
                                        id = if (isExpanded) ScheduleTheme.colors.imageArrowUp 
                                             else ScheduleTheme.colors.imageArrowDown
                                    ),
                                    contentDescription = null,
                                    tint = ScheduleTheme.colors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (isExpanded) {
                            items(courseGroups) { group ->
                                val isSelected = selectedInDialog.contains(group.name)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(
                                            if (isSelected) ScheduleTheme.colors.chipsSelect 
                                            else ScheduleTheme.colors.surface
                                        )
                                        .clickable {
                                            selectedInDialog = if (isSelected) {
                                                selectedInDialog - group.name
                                            } else {
                                                selectedInDialog + group.name
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = group.name,
                                        style = ScheduleTheme.typography.bodyMain,
                                        color = ScheduleTheme.colors.textPrimary,
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedInDialog.isNotEmpty()) {
                    Button(
                        onClick = { onGroupsAdded(selectedInDialog.toList()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScheduleTheme.colors.accent)
                    ) {
                        Text("Готово", color = Color.White, style = ScheduleTheme.typography.bodyMain)
                    }
                }
            }
        }
    }
}
