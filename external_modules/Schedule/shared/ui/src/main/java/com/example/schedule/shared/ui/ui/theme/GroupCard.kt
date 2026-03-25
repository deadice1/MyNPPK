package com.example.schedule.shared.ui.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedule.shared.group.domain.entity.Group

@Composable
fun GroupCard(
    groups: List<Group>,
    selectedGroups: List<Group>,
    onGroupClick: (Group) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (groups.isEmpty()) return

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            val isSelected = selectedGroups.any { it.id == group.id }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onGroupClick(group) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        ScheduleTheme.colors.surfaceActive
                    } else {
                        ScheduleTheme.colors.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
}

