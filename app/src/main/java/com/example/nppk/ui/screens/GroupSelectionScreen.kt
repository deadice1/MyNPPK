package com.example.nppk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.group.domain.entity.Group
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme

@Composable
fun GroupSelectionScreen(
    groups: List<Group>,
    onGroupsSelected: (List<Group>) -> Unit,
    onSkip: () -> Unit
) {
    var selectedGroups by remember { mutableStateOf(emptyList<Group>()) }
    val expandedCourses = remember { mutableStateListOf<Int>() }

    // Группируем по первому символу названия (курс)
    val groupsByCourse = remember(groups) {
        groups.groupBy { group ->
            group.name.firstOrNull()?.digitToIntOrNull() ?: 0
        }.toSortedMap()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добро пожаловать!",
                    style = ScheduleTheme.typography.h2,
                    color = ScheduleTheme.colors.textPrimary
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пропустить",
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textPrimary,
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = ScheduleTheme.colors.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Выбери одну или несколько групп",
                    style = ScheduleTheme.typography.h3,
                    color = ScheduleTheme.colors.accent,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            groupsByCourse.forEach { (course, courseGroups) ->
                val isExpanded = expandedCourses.contains(course)
                
                // Заголовок курса
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

                // Группы курса (если развернуто)
                if (isExpanded) {
                    items(courseGroups) { group ->
                        val isSelected = group in selectedGroups
                        Box(
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(percent = 50))
                                .background(
                                    if (isSelected) ScheduleTheme.colors.chipsSelect 
                                    else ScheduleTheme.colors.surface
                                )
                                .clickable {
                                    selectedGroups = if (isSelected) {
                                        selectedGroups - group
                                    } else {
                                        selectedGroups + group
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

            if (selectedGroups.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Button(
                        onClick = { onGroupsSelected(selectedGroups) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScheduleTheme.colors.accent
                        )
                    ) {
                        Text("Готово", color = Color.White, style = ScheduleTheme.typography.bodyMain)
                    }
                }
            }
        }
    }
}
