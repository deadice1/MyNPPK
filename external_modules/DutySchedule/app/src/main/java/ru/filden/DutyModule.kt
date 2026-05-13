// DutyScheduleApp.kt
package ru.filden

import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                val groups = apiClient.getAvailableGroupsForUser(userId, role)
                availableGroups = groups
                currentGroupId = groups.firstOrNull()?.id
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (userRole == null || currentGroupId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ошибка загрузки данных пользователя")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание дежурств") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
        }
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
    currentGroupId: Int?,
    onGroupSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Выбрать группу")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        onGroupSelected(group.id)
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
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Дежурства") },
            label = { Text("Дежурства") },
            selected = navController.currentDestination?.route == "main",
            onClick = { navController.navigate("main") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "История") },
            label = { Text("История") },
            selected = navController.currentDestination?.route == "history",
            onClick = { navController.navigate("history") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Студенты") },
            label = { Text("Студенты") },
            selected = navController.currentDestination?.route == "students",
            onClick = { navController.navigate("students") }
        )
    }
}