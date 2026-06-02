package ru.filden.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.filden.ui.theme.MapBlueLight
import ru.filden.ui.theme.MapBlueStroke

@Composable
fun DutyCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Shadow/Offset layer like in map_bubble_cloud_bg.xml
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = (-4).dp, y = 4.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MapBlueLight.copy(alpha = 0.5f))
        )

        // Main Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0F0FF), // startColor
                            Color(0xFFF5FBFF)  // endColor
                        )
                    )
                )
                .border(1.dp, MapBlueStroke, RoundedCornerShape(40.dp))
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DutyScreenHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(content = actions)
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
