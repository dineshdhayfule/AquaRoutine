package com.example.alarm.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alarm.model.AlarmItem

@Composable
fun AlarmItemRow(
    alarm: AlarmItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onToggleDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.timeString,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DaySelectionRow(
                    selectedDays = alarm.daysOfWeek,
                    onToggleDay = onToggleDay,
                    enabled = alarm.isActive
                )
            }

            if (alarm.isCustom) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Switch(
                checked = alarm.isActive,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun DaySelectionRow(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(modifier = modifier) {
        dayLabels.forEachIndexed { index, label ->
            val dayNumber = index + 1 // 1=Sun, 2=Mon, ..., 7=Sat
            val isSelected = selectedDays.contains(dayNumber)
            
            DayIndicator(
                label = label,
                isSelected = isSelected,
                onClick = { if (enabled) onToggleDay(dayNumber) },
                modifier = Modifier.padding(end = 4.dp),
                enabled = enabled
            )
        }
    }
}

@Composable
fun DayIndicator(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(28.dp),
        shape = CircleShape,
        color = when {
            !enabled && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when {
            !enabled && isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
