package ru.filden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch
import ru.filden.api.ApiClient
import ru.filden.api.UserRole
import ru.filden.api.canChangeGroup
import ru.filden.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyModule(
    userId: Int,
    baseUrl: String
) {
    val apiClient = remember { ApiClient(baseUrl) }
    val navController = rememberNavController()

    var userRole by remember { mutableStateOf<UserRole?>(null) }
    var currentGroupId by remember { mutableStateOf<Int?>(null) }
    var availableGroups by remember { mutableStateOf<List<ru.filden.api.ApiGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        scope.launch {
            val role = apiClient.getUserRole(userId)
            userRole = role

            if (role != null) {
                availableGroups = apiClient.getAllGroups()
                val student = apiClient.getStudentById(userId)
                if(student!=null){
                currentGroupId = student.groupId
                }
                else{
                    currentGroupId = 1
                }
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(ScheduleTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ScheduleTheme.colors.accent)
        }
        return
    }

    if (userRole == null || currentGroupId == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(ScheduleTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Ошибка загрузки данных пользователя", color = ScheduleTheme.colors.textPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Расписание дежурств",
                        style = ScheduleTheme.typography.h2,
                        color = ScheduleTheme.colors.textPrimary
                    )

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScheduleTheme.colors.surface
                ),
                actions = {
                    if (userRole?.canChangeGroup() == true && availableGroups.size > 1) {
                        GroupSelector(
                            groups = availableGroups,
                            currentGroupId = currentGroupId,
                            onGroupSelected = { currentGroupId = it }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = userRole!!
            )
        },
        containerColor = ScheduleTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainDutyScreen(
                        apiClient = apiClient,
                        groupId = currentGroupId!!,
                        userRole = userRole!!
                    )
                }
                composable("history") {
                    HistoryScreen(
                        apiClient = apiClient,
                        groupId = currentGroupId!!
                    )
                }
                composable("students") {
                    StudentsScreen(
                        apiClient = apiClient,
                        groupId = currentGroupId!!,
                        userRole = userRole!!
                    )
                }
            }
        }
    }
}

@Composable
fun GroupSelector(
    groups: List<ru.filden.api.ApiGroup>,
    currentGroupId: Int? = 1,
    onGroupSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var currentGroup by remember { mutableStateOf<ru.filden.api.ApiGroup>(groups.get(0)) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(text = "Группа: ${currentGroup.name}")
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Выбрать группу",
                tint = ScheduleTheme.colors.textPrimary
            )

        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ScheduleTheme.colors.surface)
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            group.name,
                            style = ScheduleTheme.typography.bodyMain,
                            color = ScheduleTheme.colors.textPrimary
                        ) 
                    },
                    onClick = {
                        onGroupSelected(group.id)
                        currentGroup = group
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    userRole: UserRole
) {
    NavigationBar(
        containerColor = ScheduleTheme.colors.surface,
        tonalElevation = 0.dp
    ) {
        val currentRoute = navController.currentDestination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Дежурства") },
            label = { Text("Дежурства", style = ScheduleTheme.typography.bodyTertiary) },
            selected = currentRoute == "main",
            onClick = { navController.navigate("main") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScheduleTheme.colors.accent,
                selectedTextColor = ScheduleTheme.colors.accent,
                unselectedIconColor = ScheduleTheme.colors.textSecondary,
                unselectedTextColor = ScheduleTheme.colors.textSecondary,
                indicatorColor = ScheduleTheme.colors.accent.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "История") },
            label = { Text("История", style = ScheduleTheme.typography.bodyTertiary) },
            selected = currentRoute == "history",
            onClick = { navController.navigate("history") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScheduleTheme.colors.accent,
                selectedTextColor = ScheduleTheme.colors.accent,
                unselectedIconColor = ScheduleTheme.colors.textSecondary,
                unselectedTextColor = ScheduleTheme.colors.textSecondary,
                indicatorColor = ScheduleTheme.colors.accent.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Студенты") },
            label = { Text("Студенты", style = ScheduleTheme.typography.bodyTertiary) },
            selected = currentRoute == "students",
            onClick = { navController.navigate("students") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ScheduleTheme.colors.accent,
                selectedTextColor = ScheduleTheme.colors.accent,
                unselectedIconColor = ScheduleTheme.colors.textSecondary,
                unselectedTextColor = ScheduleTheme.colors.textSecondary,
                indicatorColor = ScheduleTheme.colors.accent.copy(alpha = 0.1f)
            )
        )
    }
}