package com.example.nppk.ui.screens

import android.content.res.ColorStateList
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.example.schedule.di.GlobalBackstackNavigatorQualifier
import com.example.schedule.feature.schedule.ui.MainTestScreen
import com.example.schedule.libs.navigation.BackstackNavigator
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import org.koin.compose.koinInject
import com.example.nppk.data.repository.AuthRepository
import ru.filden.DutyModule


/**
 * Экран с расписанием (подключен напрямую к модулю Schedule).
 */
@Composable
fun ScheduleModuleScreen() {
    val navigator: BackstackNavigator = koinInject(qualifier = GlobalBackstackNavigatorQualifier)
    val authRepository: AuthRepository = koinInject()
    val isTeacherRole = remember { authRepository.getCachedRole() == "Преподаватель" }

    LaunchedEffect(navigator) {
        navigator.popToRoot()
        navigator.open(MainTestScreen(isTeacherRole = isTeacherRole))
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
    val backgroundColor = ScheduleTheme.colors.background.toArgb()
    val surfaceColor = ScheduleTheme.colors.surface.toArgb()
    val textPrimaryColor = ScheduleTheme.colors.textPrimary.toArgb()
    val textSecondaryColor = ScheduleTheme.colors.textSecondary.toArgb()
    val accentColor = ScheduleTheme.colors.accent.toArgb()
    val chipsSelectedColor = ScheduleTheme.colors.chipsSelect.toArgb()
    val dividerColor = ScheduleTheme.colors.divider.toArgb()

    AndroidView(
        factory = { ctx ->
            inflateMapRoot(ctx)
        },
        update = { rootView ->
            rootView.setBackgroundColor(backgroundColor)
            if (rootView.getTag(com.example.coll.R.id.floorWebView) == true) return@AndroidView

            val floorTitleText = rootView.findViewById<TextView>(com.example.coll.R.id.floorTitleText)
            val floorToggleGroup =
                rootView.findViewById<MaterialButtonToggleGroup>(com.example.coll.R.id.floorToggleGroup)
            val floorWebView = rootView.findViewById<WebView>(com.example.coll.R.id.floorWebView)
            val basementButton = rootView.findViewById<MaterialButton>(com.example.coll.R.id.btnFloorBasement)
            val floor1Button = rootView.findViewById<MaterialButton>(com.example.coll.R.id.btnFloor1)
            val floor2Button = rootView.findViewById<MaterialButton>(com.example.coll.R.id.btnFloor2)
            val floor3Button = rootView.findViewById<MaterialButton>(com.example.coll.R.id.btnFloor3)

            floorTitleText.setTextColor(textPrimaryColor)

            val floorButtons = listOf(basementButton, floor1Button, floor2Button, floor3Button)
            floorButtons.forEach { button ->
                button.strokeColor = ColorStateList.valueOf(dividerColor)
                button.strokeWidth = 2
                button.setTextColor(textSecondaryColor)
            }

            fun applyFloorButtonState(checkedId: Int) {
                floorButtons.forEach { button ->
                    val isSelected = button.id == checkedId
                    button.backgroundTintList = ColorStateList.valueOf(
                        if (isSelected) chipsSelectedColor else surfaceColor
                    )
                    button.strokeColor = ColorStateList.valueOf(
                        if (isSelected) accentColor else dividerColor
                    )
                    button.setTextColor(if (isSelected) backgroundColor else textSecondaryColor)
                }
            }

            floorWebView.setBackgroundColor(surfaceColor)
            floorWebView.clipToOutline = true
            (floorWebView.parent as? MaterialCardView)?.apply {
                setCardBackgroundColor(surfaceColor)
                radius = 24f
                strokeColor = dividerColor
                strokeWidth = 2
            }

            floorWebView.settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
            }
            floorWebView.setInitialScale(1)
            floorWebView.isVerticalScrollBarEnabled = false
            floorWebView.isHorizontalScrollBarEnabled = false

            floorWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript(
                        """
                            (function() {
                                var svg = document.querySelector('svg');
                                if (svg) {
                                    svg.setAttribute('width', '100%');
                                    svg.setAttribute('height', '100%');
                                    // Stretch to fill available width/height, removing side gaps.
                                    svg.setAttribute('preserveAspectRatio', 'none');
                                }
                            })();
                        """.trimIndent(),
                        null
                    )
                }
            }

            fun loadFloor(title: String, assetFile: String) {
                floorTitleText.text = title
                floorWebView.loadUrl("file:///android_asset/$assetFile")
            }

            floorToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                applyFloorButtonState(checkedId)
                when (checkedId) {
                    com.example.coll.R.id.btnFloorBasement -> loadFloor("Подвал", "floor_basement.svg")
                    com.example.coll.R.id.btnFloor1 -> loadFloor("1 этаж", "floor1.svg")
                    com.example.coll.R.id.btnFloor2 -> loadFloor("2 этаж", "floor2.svg")
                    com.example.coll.R.id.btnFloor3 -> loadFloor("3 этаж", "floor3.svg")
                }
            }

            floorToggleGroup.check(com.example.coll.R.id.btnFloor2)
            applyFloorButtonState(com.example.coll.R.id.btnFloor2)
            rootView.setTag(com.example.coll.R.id.floorWebView, true)
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    )
}

private fun inflateMapRoot(ctx: Context): View {
    return LayoutInflater.from(ctx).inflate(com.example.coll.R.layout.activity_main, null, false)
}

/**
 * Экран дежурств (используем Compose-функции из модуля DutySchedule).
 */
@Composable
fun DutyScheduleModuleScreen(ctx: Context) {
    DutyModule(ctx.getSharedPreferences("nppk_prefs",Context.MODE_PRIVATE).getInt("user_id", 1),"http://80.89.199.85:8081")

}

