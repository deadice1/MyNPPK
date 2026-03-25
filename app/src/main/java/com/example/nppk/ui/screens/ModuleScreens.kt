package com.example.nppk.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.schedule.di.GlobalBackstackNavigatorQualifier
import com.example.schedule.feature.schedule.ui.MainTestScreen
import com.example.schedule.libs.navigation.BackstackNavigator
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.compose.koinInject
import ru.filden.MainApp
import ru.filden.logic.ScheduleController
import ru.filden.logic.UserRole

/**
 * Экран с расписанием (подключен напрямую к модулю Schedule).
 */
@Composable
fun ScheduleModuleScreen() {
    val navigator: BackstackNavigator = koinInject(qualifier = GlobalBackstackNavigatorQualifier)

    LaunchedEffect(navigator) {
        navigator.popToRoot()
        navigator.open(MainTestScreen())
    }

    val currentScreen by navigator.currentScreen.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScheduleTheme.colors.background
    ) {
        currentScreen.Render()
    }
}

/**
 * Экран карты из модуля Map (coll). Мы напрямую надуваем его layout.
 */
@Composable
fun MapModuleScreen() {
    AndroidView(
        factory = { ctx ->
            com.example.coll.ui.SvgMapView(ctx).apply {
                post {
                    loadSvgFromAssets("floor2.svg")
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Экран дежурств (используем Compose-функции из модуля DutySchedule).
 */
@Composable
fun DutyScheduleModuleScreen() {
    val controller = remember { ScheduleController() }

    MainApp(
        controller = controller,
        initialGroup = "1",
        userRole = UserRole.STUDENT,
    )
}

