package ru.filden

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.filden.logic.ScheduleController
import ru.filden.logic.UserRole

class selectGroupActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val controller = remember { ScheduleController() }
            SelectGroupContent(controller)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectGroupContent(controller: ScheduleController) {
    val context = LocalContext.current
    var selectedGroup by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val groups = remember { controller.getAllGroups() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход в систему") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Расписание дежурств",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            GroupSelectionCard(
                groups = groups,
                selectedGroup = selectedGroup,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onGroupSelected = { group ->
                    selectedGroup = group
                    expanded = false
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (selectedGroup.isNotEmpty()) {
                Text(
                    text = "Войти как:",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                RoleButtons(
                    selectedGroup = selectedGroup,
                    onRoleSelected = { role ->
                        selectedRole = role
                    }
                )
            }
            selectedRole?.let { role ->
                Spacer(modifier = Modifier.height(24.dp))

                EnterButton(
                    selectedGroup = selectedGroup,
                    role = role,
                    onEnter = { group, userRole ->
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra("currentGroup", group)
                            putExtra("userRole", userRole.name)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionCard(
    groups: List<String>,
    selectedGroup: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGroupSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Выберите группу",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = if (selectedGroup.isNotEmpty()) "Группа $selectedGroup" else "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Нажмите для выбора") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    groups.forEach { groupId ->
                        DropdownMenuItem(
                            text = { Text("Группа $groupId") },
                            onClick = { onGroupSelected(groupId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleButtons(
    selectedGroup: String,
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        UserRole.entries.forEach { role ->
            RoleButton(
                role = role,
                isEnabled = selectedGroup.isNotEmpty(),
                onClick = { onRoleSelected(role) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RoleButton(
    role: UserRole,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = role.displayName,
                fontSize = 16.sp
            )
            Text(
                text = role.description,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun EnterButton(
    selectedGroup: String,
    role: UserRole,
    onEnter: (String, UserRole) -> Unit
) {
    Button(
        onClick = { onEnter(selectedGroup, role) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Войти как ${role.displayName}",
            fontSize = 18.sp
        )
    }
}