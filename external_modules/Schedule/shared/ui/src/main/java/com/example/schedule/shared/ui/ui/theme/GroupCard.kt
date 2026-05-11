package com.example.schedule.shared.ui.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.group.domain.entity.Group

@Composable
fun GroupCard(
    groups: List<Group>,
    onGroupClick: (Group) -> Unit,
    selectedGroups: List<Group> = emptyList(),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups) { group ->
            val isSelected = group in selectedGroups
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (isSelected) ScheduleTheme.colors.chipsSelect else ScheduleTheme.colors.surface
                    )
                    .clickable { onGroupClick(group) }
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    .wrapContentHeight(),
            contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name,
                    style = ScheduleTheme.typography.bodyMain,
                    color = ScheduleTheme.colors.textPrimary,
                )
            }
        }
    }
}