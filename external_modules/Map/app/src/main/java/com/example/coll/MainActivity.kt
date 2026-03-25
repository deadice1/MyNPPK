package com.example.coll

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.schedule.shared.ui.ui.theme.ScheduleTheme
import com.example.coll.ui.SvgMapView
import com.example.coll.ui.SvgMapHelper
import com.example.coll.data.RoomArea

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScheduleTheme {
                MapModuleScreen()
            }
        }
    }
}

@Composable
fun MapModuleScreen() {
    var debugMode by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScheduleTheme.colors.background)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2 этаж",
                style = ScheduleTheme.typography.h1,
                color = ScheduleTheme.colors.textPrimary
            )
            Button(
                onClick = { debugMode = !debugMode },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (debugMode) ScheduleTheme.colors.surfaceActive else ScheduleTheme.colors.accent
                )
            ) {
                Text(text = if (debugMode) "Выкл. отладку" else "Режим отладки", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    SvgMapView(ctx).apply {
                        post {
                            try {
                                loadSvgFromAssets("floor2.svg")
                                setRoomAreas(getRoomAreas())
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                        onRoomClickListener = { roomId ->
                            if (!debugMode) {
                                Toast.makeText(ctx, "Кабинет: $roomId", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onDebugClickListener = { x, y -> debugInfo = "X=${x.toInt()}, Y=${y.toInt()}" }
                    }
                },
                update = { view -> view.setDebugMode(debugMode) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (debugMode) debugInfo.ifEmpty { "Кликайте для координат" }
            else "Нажмите на кабинет для просмотра расписания",
            style = ScheduleTheme.typography.bodySecondary,
            color = ScheduleTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getRoomAreas(): List<RoomArea> {
    return listOf(
        SvgMapHelper.createRectArea("room_201", 213f, 585f, 380f, 816.5f),
        SvgMapHelper.createRectArea("room_202", 380f, 585f, 446.5f, 816.5f),
        SvgMapHelper.createRectArea("room_203", 446.5f, 585f, 495.5f, 816.5f),
        SvgMapHelper.createRectArea("room_204", 495.5f, 585f, 605.5f, 816.5f),
        SvgMapHelper.createRectArea("room_205", 653f, 585f, 802.5f, 816.5f),
        SvgMapHelper.createRectArea("room_206", 802.5f, 585f, 933.5f, 816.5f),
        SvgMapHelper.createRectArea("room_207", 933.5f, 585f, 1155f, 816.5f),
        SvgMapHelper.createRectArea("room_208", 1155f, 585f, 1285f, 816.5f),
        SvgMapHelper.createRectArea("room_209", 1309.5f, 585f, 1521.5f, 816.5f),
        SvgMapHelper.createRectArea("room_sports", 331f, 116.5f, 575.5f, 585f)
    )
}