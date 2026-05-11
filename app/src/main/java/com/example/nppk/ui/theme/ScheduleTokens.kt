package com.example.nppk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.schedule.feature.schedule.R
import com.example.schedule.shared.ui.ui.theme.ColorScheme as ScheduleColorScheme
import com.example.schedule.shared.ui.ui.theme.Typography as ScheduleTypography

private val Black = Color(0xFF212121)
private val White = Color(0xFFFFFFFF)
private val Secondary = Color(0xFF828282)
private val Accent = Color(0xFF475E90)
private val Red = Color(0xFFFF7676)

private val SurfaceLight = Color(0xFFF4F5F7)
private val SurfaceDark = Color(0xFF2E2F33)
private val SurfaceActive = Accent.copy(alpha = 0.20f)
private val BackgroundDark = Color(0xFF121214)

private val Divider = Secondary.copy(alpha = 0.40f)

private val PressedButtonLight = Color(0xFFE8E8EA)
private val PressedButtonDark = Color(0xFF232227)

private val ChipsSelect = Color(0xFF849CD0)

private val ScheduleTypographyTokens = ScheduleTypography(
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    h4 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodyMain = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodySecondary = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyTertiary = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun scheduleColorSchemeFromMaterial(
    darkTheme: Boolean
): ScheduleColorScheme {
    // Keep exactly the same palette as Schedule module.
    // We only move the source of tokens into the host app.
    return if (darkTheme) {
        ScheduleColorScheme(
            textPrimary = White,
            textSecondary = Secondary,
            accent = Accent,
            error = Red,
            background = BackgroundDark,
            surface = SurfaceDark,
            surfaceActive = SurfaceActive,
            divider = Divider,
            pressedButton = PressedButtonDark,
            chipsSelect = ChipsSelect,

            imageNoSchedule = R.drawable.no_lessons_dark,
            imageNoGroup = R.drawable.no_group_dark,
            imageWeekend = R.drawable.weekend,
            imageCalendar = R.drawable.calendar_light,
            imageCalendarClicked = R.drawable.calendar_light,
            imageHome = R.drawable.home_light,
            imageHomeClicked = R.drawable.home_light,
            imageSettings = R.drawable.settings_light,
            imageSettingsClicked = R.drawable.settings_light,
            imageArrowRight = R.drawable.arrow_right_lite,
            imageArrowLeft = R.drawable.arrow_left_lite,
            imageArrowDown = R.drawable.arrow_down_lite,
            imageArrowUp = R.drawable.arrow_up_lite,
        )
    } else {
        ScheduleColorScheme(
            textPrimary = Black,
            textSecondary = Secondary,
            accent = Accent,
            error = Red,
            background = White,
            surface = SurfaceLight,
            surfaceActive = SurfaceActive,
            divider = Divider,
            pressedButton = PressedButtonLight,
            chipsSelect = ChipsSelect,

            imageNoSchedule = R.drawable.no_lessons_light,
            imageNoGroup = R.drawable.no_group_light,
            imageWeekend = R.drawable.weekend,
            imageCalendar = R.drawable.calendar_dark,
            imageCalendarClicked = R.drawable.calendar_light,
            imageHome = R.drawable.home_dark,
            imageHomeClicked = R.drawable.home_light,
            imageSettings = R.drawable.settings_dark,
            imageSettingsClicked = R.drawable.settings_light,
            imageArrowRight = R.drawable.arrow_right_dark,
            imageArrowLeft = R.drawable.arrow_left_dark,
            imageArrowDown = R.drawable.arrow_down_dark,
            imageArrowUp = R.drawable.arrow_up_dark,
        )
    }
}

@Composable
fun scheduleTypographyFromMaterial(): ScheduleTypography {
    // Keep exactly the same typography as Schedule module.
    return ScheduleTypographyTokens
}

