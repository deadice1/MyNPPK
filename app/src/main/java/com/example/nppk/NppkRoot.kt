package com.example.nppk

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.nppk.ui.screens.DutyScheduleModuleScreen
import com.example.nppk.ui.screens.GroupSelectionScreen
import com.example.nppk.ui.screens.MapModuleScreen
import com.example.nppk.ui.screens.ScheduleModuleScreen
import com.example.nppk.ui.viewmodels.GroupSelectionViewModel
import com.example.nppk.ui.viewmodels.MyGroupsViewModel
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import com.example.nppk.ui.theme.scheduleColorSchemeFromMaterial
import com.example.nppk.ui.theme.scheduleTypographyFromMaterial
import com.example.schedule.shared.ui.ui.theme.ProvideScheduleTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nppk.data.repository.AuthRepository
import com.example.schedule.shared.ui.util.NotificationHelper

enum class AuthMode {
    UNAUTHENTICATED,
    SELECTING_GROUPS,
    GUEST,
    AUTHENTICATED
}

// ---------------------------------------------------------------------------
// Двойной "назад" для выхода из приложения.
// ---------------------------------------------------------------------------
@Composable
private fun DoubleBackToExit(timeoutMs: Long = 2000L) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < timeoutMs) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun NppkMainContent() {
    val context = LocalContext.current
    val view = LocalView.current

    // Запрос разрешений на уведомления для Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Без разрешения уведомления приходить не будут", Toast.LENGTH_SHORT).show()
            }
        }
        
        LaunchedEffect(Unit) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var isDarkTheme by rememberSaveable { mutableStateOf(readDarkThemePreference(context)) }
    var isNotificationsEnabled by rememberSaveable { mutableStateOf(readNotificationsEnabledPreference(context)) }
    val authRepository: AuthRepository = koinInject()
    var authMode by rememberSaveable { 
        mutableStateOf(
            when {
                readIsLoggedIn(context) -> {
                    if (authRepository.isTeacherFirstLogin()) AuthMode.SELECTING_GROUPS 
                    else AuthMode.AUTHENTICATED
                }
                else -> AuthMode.UNAUTHENTICATED
            }
        ) 
    }
    var openGuestOnMap by rememberSaveable { mutableStateOf(false) }

    val scheduleColors = scheduleColorSchemeFromMaterial(darkTheme = isDarkTheme)
    val scheduleTypography = scheduleTypographyFromMaterial()

    ProvideScheduleTheme(colors = scheduleColors, typography = scheduleTypography) {
        val backgroundColor = ScheduleTheme.colors.background.toArgb()

        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = backgroundColor
                window.navigationBarColor = backgroundColor
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }
        }

        when (authMode) {
            AuthMode.UNAUTHENTICATED -> {
                LoginScreen(
                    onLogin = { authMode = AuthMode.AUTHENTICATED },
                    onLoginAsGuest = {
                        authMode = AuthMode.GUEST
                        openGuestOnMap = true
                    },
                    onTeacherFirstLogin = {
                        authMode = AuthMode.SELECTING_GROUPS
                    }
                )
            }

            AuthMode.SELECTING_GROUPS -> {
                val viewModel: MyGroupsViewModel = koinViewModel()
                val authRepository: AuthRepository = koinInject()
                val groups by viewModel.groups.collectAsState()

                GroupSelectionScreen(
                    groups = groups,
                    onGroupsSelected = { selectedGroups ->
                        viewModel.addGroups(selectedGroups.map { it.name })
                        authRepository.setTeacherFirstLoginCompleted()
                        authMode = AuthMode.AUTHENTICATED
                    },
                    onSkip = {
                        authRepository.setTeacherFirstLoginCompleted()
                        authMode = AuthMode.AUTHENTICATED
                    }
                )
            }

            AuthMode.GUEST,
            AuthMode.AUTHENTICATED -> {
                DoubleBackToExit()

                MainScaffold(
                    authMode = authMode,
                    onAuthenticated = { authMode = AuthMode.AUTHENTICATED },
                    openGuestOnMap = openGuestOnMap,
                    onGuestMapOpened = { openGuestOnMap = false },
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = { enabled ->
                        isDarkTheme = enabled
                        saveDarkThemePreference(context, enabled)
                        showThemeChangeNotification(context, enabled)
                    },
                    isNotificationsEnabled = isNotificationsEnabled,
                    onNotificationsEnabledChange = { enabled ->
                        isNotificationsEnabled = enabled
                        saveNotificationsEnabledPreference(context, enabled)
                    },
                    onLogout = {
                        authMode = AuthMode.UNAUTHENTICATED
                        openGuestOnMap = false
                    }
                )
            }
        }
    }
}

@Immutable
data class BottomNavItem(
    val title: String,
    val iconRes: Int? = null,
    val selectedIconRes: Int? = null,
    val fallbackIcon: ImageVector? = null
)

private const val PREFS_NAME = "nppk_prefs"
private const val KEY_DARK_THEME = "dark_theme_enabled"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val KEY_IS_LOGGED_IN = "is_logged_in"

fun readDarkThemePreference(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_DARK_THEME, false)

fun readNotificationsEnabledPreference(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

fun readIsLoggedIn(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_IS_LOGGED_IN, false)

private fun saveDarkThemePreference(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_DARK_THEME, enabled).apply()
}

private fun saveNotificationsEnabledPreference(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
}

fun showThemeChangeNotification(context: Context, isDark: Boolean) {
    val themeName = if (isDark) "Тёмная" else "Светлая"
    NotificationHelper.showNotification(
        context = context,
        title = "Тема изменена",
        message = "Установлена $themeName тема оформления",
        notificationId = 1
    )
}

// Хранит направление последнего перехода — нужно для анимации слайда
private enum class NavDirection { LEFT, RIGHT }

@Composable
fun MainScaffold(
    authMode: AuthMode,
    onAuthenticated: () -> Unit,
    openGuestOnMap: Boolean,
    onGuestMapOpened: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val navItems: List<BottomNavItem> =
        when (authMode) {
            AuthMode.GUEST -> listOf(
                BottomNavItem(title = "Профиль", fallbackIcon = Icons.Outlined.Person),
                BottomNavItem(title = "Карта", fallbackIcon = Icons.Outlined.Map)
            )
            AuthMode.AUTHENTICATED -> listOf(
                BottomNavItem(
                    title = "Расписание", // Добавили текст
                    iconRes = ScheduleTheme.colors.imageCalendar,
                    selectedIconRes = ScheduleTheme.colors.imageCalendarClicked
                ),
                BottomNavItem(title = "Карта", fallbackIcon = Icons.Outlined.Map),
                BottomNavItem(title = "Дежурства", iconRes = R.drawable.ic_cleaning),
                BottomNavItem(
                    title = "Настройки",
                    iconRes = ScheduleTheme.colors.imageSettings,
                    selectedIconRes = ScheduleTheme.colors.imageSettingsClicked
                )
            )
            else -> emptyList()
        }

    // Текущая страница и направление анимации
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var navDirection by remember { mutableStateOf(NavDirection.RIGHT) }
    var isMyGroupsVisible by rememberSaveable { mutableStateOf(false) }

    val density = LocalDensity.current
    val view = LocalView.current

    val edgeSwipeZoneDp = 52.dp
    val swipeThresholdPx = with(density) { 40.dp.toPx() }
    val edgeZonePx = with(density) { edgeSwipeZoneDp.toPx() }.toInt()

    // Переключение страницы — всегда через эту функцию, чтобы направление запомнилось
    fun navigateTo(index: Int) {
        if (index == currentPage) return
        navDirection = if (index > currentPage) NavDirection.RIGHT else NavDirection.LEFT
        currentPage = index.coerceIn(0, (navItems.size - 1).coerceAtLeast(0))
    }

    // Исключаем edge-зоны из системных жестов Android (только там, где есть куда листать)
    LaunchedEffect(currentPage, navItems.size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rects = mutableListOf<android.graphics.Rect>()
            val w = view.width
            val h = view.height
            if (currentPage > 0)
                rects.add(android.graphics.Rect(0, 0, edgeZonePx, h))
            if (currentPage < navItems.size - 1)
                rects.add(android.graphics.Rect(w - edgeZonePx, 0, w, h))
            view.systemGestureExclusionRects = rects
        }
    }

    LaunchedEffect(authMode, openGuestOnMap) {
        if (authMode == AuthMode.GUEST && openGuestOnMap && navItems.size > 1) {
            navigateTo(1)
            onGuestMapOpened()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (authMode == AuthMode.AUTHENTICATED && currentPage == (navItems.size - 1) && isMyGroupsVisible) {
                BackHandler { isMyGroupsVisible = false }
                com.example.nppk.ui.screens.MyGroupsScreen(
                    onBack = { isMyGroupsVisible = false }
                )
            } else {
                // ---------------------------------------------------------------
                // AnimatedContent вместо HorizontalPager.
                // ---------------------------------------------------------------
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        val animDuration = 300
                        if (navDirection == NavDirection.RIGHT) {
                            slideInHorizontally(
                                animationSpec = tween(animDuration),
                                initialOffsetX = { it }
                            ) togetherWith slideOutHorizontally(
                                animationSpec = tween(animDuration),
                                targetOffsetX = { -it }
                            )
                        } else {
                            slideInHorizontally(
                                animationSpec = tween(animDuration),
                                initialOffsetX = { -it }
                            ) togetherWith slideOutHorizontally(
                                animationSpec = tween(animDuration),
                                targetOffsetX = { it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "page_transition"
                ) { page ->
                    when (authMode) {
                        AuthMode.GUEST -> when (page) {
                            0 -> LoginScreen(
                                onLogin = { onAuthenticated() },
                                onLoginAsGuest = { },
                                onTeacherFirstLogin = { } // Не актуально для гостя
                            )
                            else -> MapModuleScreen()
                        }
                        AuthMode.AUTHENTICATED -> when (page) {
                            0 -> ScheduleModuleScreen()
                            1 -> MapModuleScreen()
                            2 -> DutyScheduleModuleScreen(LocalContext.current)
                            else -> SettingsScreen(
                                isDarkTheme = isDarkTheme,
                                onDarkThemeChange = onDarkThemeChange,
                                isNotificationsEnabled = isNotificationsEnabled,
                                onNotificationsEnabledChange = onNotificationsEnabledChange,
                                onLogout = { onLogout() },
                                onOpenMyGroups = { isMyGroupsVisible = true }
                            )
                        }
                        AuthMode.UNAUTHENTICATED,
                        AuthMode.SELECTING_GROUPS -> Box(Modifier.fillMaxSize())
                    }
                }
            }

            // Левая edge-зона — свайп вправо → предыдущая вкладка
            if (currentPage > 0 && !isMyGroupsVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(edgeSwipeZoneDp)
                        .pointerInput(currentPage) {
                            var totalDx = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDx = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    totalDx += dragAmount
                                    change.consume()
                                },
                                onDragEnd = {
                                    if (totalDx > swipeThresholdPx) {
                                        navigateTo(currentPage - 1)
                                    }
                                }
                            )
                        }
                )
            }

            // Правая edge-зона — свайп влево → следующая вкладка
            if (currentPage < navItems.size - 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(edgeSwipeZoneDp)
                        .pointerInput(currentPage) {
                            var totalDx = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDx = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    totalDx += dragAmount
                                    change.consume()
                                },
                                onDragEnd = {
                                    if (totalDx < -swipeThresholdPx) {
                                        navigateTo(currentPage + 1)
                                    }
                                }
                            )
                        }
                )
            }
        }

        BottomNavigationBar(
            items = navItems,
            selectedIndex = currentPage,
            onItemSelected = { navigateTo(it) }
        )
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScheduleTheme.colors.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .navigationBarsPadding(), // <--- ДОБАВЬ ЭТО!
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            // Определяем цвет иконки
            val iconColor = if (isSelected) ScheduleTheme.colors.textPrimary else ScheduleTheme.colors.textSecondary

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) ScheduleTheme.colors.chipsSelect
                        else Color.Transparent
                    )
                    .clickable { onItemSelected(index) }
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ВЕЗДЕ используем Icon вместо Image
                if (item.iconRes != null) {
                    val drawableRes = if (isSelected && item.selectedIconRes != null)
                        item.selectedIconRes else item.iconRes

                    Icon(
                        painter = painterResource(id = drawableRes),
                        contentDescription = item.title,
                        tint = iconColor // Принудительно красим иконку!
                    )
                } else if (item.fallbackIcon != null) {
                    Icon(
                        imageVector = item.fallbackIcon,
                        contentDescription = item.title,
                        tint = iconColor // Красим вектор
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.title,
                    style = ScheduleTheme.typography.bodySecondary.copy(fontSize = 10.sp),
                    color = iconColor, // Текст тоже красим так же
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}