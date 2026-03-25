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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.core.view.WindowCompat
import com.example.nppk.ui.screens.DutyScheduleModuleScreen
import com.example.nppk.ui.screens.MapModuleScreen
import com.example.nppk.ui.screens.ScheduleModuleScreen
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import com.example.nppk.ui.theme.scheduleColorSchemeFromMaterial
import com.example.nppk.ui.theme.scheduleTypographyFromMaterial
import com.example.schedule.shared.ui.ui.theme.ProvideScheduleTheme

enum class AuthMode {
    UNAUTHENTICATED,
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

    var isDarkTheme by rememberSaveable { mutableStateOf(readDarkThemePreference(context)) }
    var authMode by rememberSaveable { mutableStateOf(AuthMode.UNAUTHENTICATED) }
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

private fun readDarkThemePreference(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_DARK_THEME, false)

private fun saveDarkThemePreference(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_DARK_THEME, enabled).apply()
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
    onLogout: () -> Unit
) {
    val navItems: List<BottomNavItem> =
        when (authMode) {
            AuthMode.GUEST -> listOf(
                BottomNavItem(title = "", fallbackIcon = Icons.Outlined.Person),
                BottomNavItem(title = "", fallbackIcon = Icons.Outlined.Map)
            )
            AuthMode.AUTHENTICATED -> listOf(
                BottomNavItem(
                    title = "",
                    iconRes = ScheduleTheme.colors.imageCalendar,
                    selectedIconRes = ScheduleTheme.colors.imageCalendarClicked
                ),
                BottomNavItem(title = "", fallbackIcon = Icons.Outlined.Map),
                BottomNavItem(title = "", fallbackIcon = Icons.Outlined.Assignment),
                BottomNavItem(
                    title = "",
                    iconRes = ScheduleTheme.colors.imageSettings,
                    selectedIconRes = ScheduleTheme.colors.imageSettingsClicked
                )
            )
            AuthMode.UNAUTHENTICATED -> emptyList()
        }

    // Текущая страница и направление анимации
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    var navDirection by remember { mutableStateOf(NavDirection.RIGHT) }

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
            // ---------------------------------------------------------------
            // AnimatedContent вместо HorizontalPager.
            // Никаких внутренних gesture detector-ов — только чистая анимация.
            // Модули внутри получают все жесты без каких-либо конфликтов.
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
                            onLoginAsGuest = { }
                        )
                        else -> MapModuleScreen()
                    }
                    AuthMode.AUTHENTICATED -> when (page) {
                        0 -> ScheduleModuleScreen()
                        1 -> MapModuleScreen()
                        2 -> DutyScheduleModuleScreen()
                        else -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onDarkThemeChange = onDarkThemeChange,
                            onLogout = { onLogout() }
                        )
                    }
                    AuthMode.UNAUTHENTICATED -> Box(Modifier.fillMaxSize())
                }
            }

            // Левая edge-зона — свайп вправо → предыдущая вкладка
            if (currentPage > 0) {
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
                        val drawable = if (isSelected && item.selectedIconRes != null)
                            item.selectedIconRes else item.iconRes
                        Image(
                            painter = painterResource(id = drawable),
                            contentDescription = item.title
                        )
                    }
                    item.fallbackIcon != null -> {
                        Icon(
                            imageVector = item.fallbackIcon,
                            contentDescription = item.title,
                            tint = if (isSelected) ScheduleTheme.colors.surface
                            else ScheduleTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = ScheduleTheme.typography.bodySecondary,
                    color = if (isSelected) ScheduleTheme.colors.surface
                    else ScheduleTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}