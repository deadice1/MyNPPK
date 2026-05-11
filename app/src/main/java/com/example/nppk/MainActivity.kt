package com.example.nppk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.nppk.ui.theme.scheduleColorSchemeFromMaterial
import com.example.nppk.ui.theme.scheduleTypographyFromMaterial
import com.example.schedule.shared.ui.ui.theme.ProvideScheduleTheme
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        splashScreen.setKeepOnScreenCondition { false }

        setContent {
            val context = LocalContext.current
            var isDarkTheme by rememberSaveable { mutableStateOf(readDarkThemePreference(context)) }

            val scheduleColors = scheduleColorSchemeFromMaterial(darkTheme = isDarkTheme)
            val scheduleTypography = scheduleTypographyFromMaterial()

            ProvideScheduleTheme(colors = scheduleColors, typography = scheduleTypography) {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 800),
                    label = "splash_crossfade"
                ) { isSplashScreen ->
                    if (isSplashScreen) {
                        NppkSplash(
                            durationMs = 2500L,
                            onFinish = { showSplash = false }
                        )
                    } else {
                        NppkMainContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun NppkSplash(
    durationMs: Long,
    onFinish: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(durationMs)
        onFinish()
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logo_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val loaderAlphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600),
        label = "loader_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // ПРИНУДИТЕЛЬНО БЕЛЫЙ ФОН
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "НППК Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .scale(scaleAnim)
                .alpha(alphaAnim)
        )

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp)
                .size(40.dp)
                .alpha(loaderAlphaAnim),
            // Используем контрастный синий цвет, чтобы лоадер был виден на белом
            color = Color(0xFF0B5ED7),
            trackColor = Color.Black.copy(alpha = 0.1f),
            strokeWidth = 3.5.dp,
            strokeCap = StrokeCap.Round
        )
    }
}