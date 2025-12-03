package com.example.nppk // ⚠️ ОСТАВЬ СВОЙ ПАКЕТ, КОТОРЫЙ БЫЛ!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nppk.ui.screens.DutyScheduleModuleScreen
import com.example.nppk.ui.screens.MapModuleScreen
import com.example.nppk.ui.screens.ScheduleModuleScreen
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScheduleTheme {
                val navItems = rememberNavItems()
                val pagerState = rememberPagerState { navItems.size }
                val scope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ScheduleTheme.colors.background)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { page ->
                        when (page) {
                            0 -> LoginScreen()
                            1 -> ScheduleModuleScreen()
                            2 -> MapModuleScreen()
                            3 -> DutyScheduleModuleScreen()
                            else -> SettingsScreen()
                        }
                    }

                    BottomNavigationBar(
                        items = navItems,
                        selectedIndex = pagerState.currentPage,
                        onItemSelected = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Immutable
private data class BottomNavItem(
    val title: String,
    val iconRes: Int? = null,
    val selectedIconRes: Int? = null,
    val fallbackIcon: ImageVector? = null
)

@Composable
private fun rememberNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        title = "Главная",
        iconRes = ScheduleTheme.colors.imageHome,
        selectedIconRes = ScheduleTheme.colors.imageHomeClicked
    ),
    BottomNavItem(
        title = "Расписание",
        iconRes = ScheduleTheme.colors.imageCalendar,
        selectedIconRes = ScheduleTheme.colors.imageCalendarClicked
    ),
    BottomNavItem(
        title = "Карта",
        fallbackIcon = Icons.Outlined.Map
    ),
    BottomNavItem(
        title = "Дежурства",
        fallbackIcon = Icons.Outlined.Assignment
    ),
    BottomNavItem(
        title = "Настройки",
        iconRes = ScheduleTheme.colors.imageSettings,
        selectedIconRes = ScheduleTheme.colors.imageSettingsClicked
    )
)

@Composable
private fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScheduleTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) ScheduleTheme.colors.chipsSelect
                        else ScheduleTheme.colors.surface
                    )
                    .clickable { onItemSelected(index) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    item.iconRes != null -> {
                        val drawable = if (isSelected && item.selectedIconRes != null) {
                            item.selectedIconRes
                        } else {
                            item.iconRes
                        }
                        Image(
                            painter = painterResource(id = drawable),
                            contentDescription = item.title
                        )
                    }

                    item.fallbackIcon != null -> {
                        Icon(
                            imageVector = item.fallbackIcon,
                            contentDescription = item.title,
                            tint = if (isSelected) ScheduleTheme.colors.surface else ScheduleTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = ScheduleTheme.typography.bodySecondary,
                    color = if (isSelected) ScheduleTheme.colors.surface else ScheduleTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LoginScreen() {
    val loginState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = loginState.value,
            onValueChange = { loginState.value = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = ScheduleTheme.typography.bodyMain,
            label = {
                Text(
                    text = "Логин",
                    style = ScheduleTheme.typography.bodySecondary
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = ScheduleTheme.typography.bodyMain,
            label = {
                Text(
                    text = "Пароль",
                    style = ScheduleTheme.typography.bodySecondary
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: логика авторизации появится позже */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.accent
            )
        ) {
            Text(
                text = "Войти",
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.surface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* TODO: логика гостевого входа */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScheduleTheme.colors.surfaceActive
            )
        ) {
            Text(
                text = "Войти как гость",
                style = ScheduleTheme.typography.bodyMain,
                color = ScheduleTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Настройки",
            style = ScheduleTheme.typography.h1,
            color = ScheduleTheme.colors.textPrimary
        )
        Text(
            text = "Экран скоро появится",
            style = ScheduleTheme.typography.bodySecondary,
            color = ScheduleTheme.colors.textSecondary
        )
    }
}