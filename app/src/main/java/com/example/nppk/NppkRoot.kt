package com.example.nppk

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.nppk.ui.screens.DutyScheduleModuleScreen
import com.example.nppk.ui.screens.MapModuleScreen
import com.example.nppk.ui.screens.ScheduleModuleScreen
import com.example.schedule.shared.ui.ui.theme.GlobalThemeConfig
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.launch

enum class AuthMode {
    UNAUTHENTICATED,
    GUEST,
    AUTHENTICATED
}

@Composable
fun NppkMainContent() {
    val context = LocalContext.current
    val view = LocalView.current

    var isDarkTheme by rememberSaveable {
        mutableStateOf(readDarkThemePreference(context))
    }
    var authMode by rememberSaveable { mutableStateOf(AuthMode.UNAUTHENTICATED) }
    var openGuestOnMap by rememberSaveable { mutableStateOf(false) }

    GlobalThemeConfig.overrideDarkTheme = isDarkTheme

    ScheduleTheme(darkTheme = isDarkTheme) {
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

private fun readDarkThemePreference(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_DARK_THEME, /* defaultValue = */ false)
}

private fun saveDarkThemePreference(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
}

@OptIn(ExperimentalFoundationApi::class)
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
                BottomNavItem(
                    title = "",
                    fallbackIcon = Icons.Outlined.Person
                ),
                BottomNavItem(
                    title = "",
                    fallbackIcon = Icons.Outlined.Map
                )
            )

            AuthMode.AUTHENTICATED -> listOf(
                BottomNavItem(
                    title = "",
                    iconRes = ScheduleTheme.colors.imageCalendar,
                    selectedIconRes = ScheduleTheme.colors.imageCalendarClicked
                ),
                BottomNavItem(
                    title = "",
                    fallbackIcon = Icons.Outlined.Map
                ),
                BottomNavItem(
                    title = "",
                    fallbackIcon = Icons.Outlined.Assignment
                ),
                BottomNavItem(
                    title = "",
                    iconRes = ScheduleTheme.colors.imageSettings,
                    selectedIconRes = ScheduleTheme.colors.imageSettingsClicked
                )
            )

            AuthMode.UNAUTHENTICATED -> emptyList()
        }

    val pagerState = rememberPagerState { navItems.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authMode, openGuestOnMap) {
        if (authMode == AuthMode.GUEST && openGuestOnMap && navItems.size > 1) {
            pagerState.scrollToPage(1)
            onGuestMapOpened()
        }
    }

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
            when (authMode) {
                AuthMode.GUEST -> {
                    when (page) {
                        0 -> LoginScreen(
                            onLogin = { onAuthenticated() },
                            onLoginAsGuest = { /* already guest, ignore */ }
                        )
                        1 -> MapModuleScreen()
                    }
                }

                AuthMode.AUTHENTICATED -> {
                    when (page) {
                        0 -> ScheduleModuleScreen()
                        1 -> MapModuleScreen()
                        2 -> DutyScheduleModuleScreen()
                        else -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onDarkThemeChange = onDarkThemeChange,
                            onLogout = {
                                // Возврат на экран логина
                                onLogout()
                            }
                        )
                    }
                }

                AuthMode.UNAUTHENTICATED -> Unit
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

