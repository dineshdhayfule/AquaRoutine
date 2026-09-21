package com.example.alarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarm.model.AlarmItem
import com.example.alarm.ui.components.AlarmItemRow
import com.example.alarm.ui.components.TimePickerDialog
import com.example.alarm.ui.theme.AlarmTheme
import com.example.alarm.viewmodel.WaterIntakeViewModel
import java.util.Calendar

@Composable
fun RoutineReminderScreen(
    viewModel: WaterIntakeViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    
    RoutineReminderContent(
        alarms = alarms,
        onToggleAlarm = viewModel::toggleAlarm,
        onDeleteAlarm = viewModel::deleteAlarm,
        onAddAlarm = viewModel::addCustomAlarm,
        onUpdateAlarm = viewModel::updateAlarm,
        onToggleMaster = viewModel::toggleMaster,
        onToggleDay = viewModel::toggleDay,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineReminderContent(
    alarms: List<AlarmItem>,
    onToggleAlarm: (AlarmItem) -> Unit,
    onDeleteAlarm: (AlarmItem) -> Unit,
    onAddAlarm: (Int, Int) -> Unit,
    onUpdateAlarm: (Int, Int, Int) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAlarms = alarms.filter { it.isActive }
    val inactiveAlarms = alarms.filter { !it.isActive }
    
    var showTimePicker by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (showTimePicker || editingAlarm != null) {
        val calendar: Calendar = remember { Calendar.getInstance() }
        TimePickerDialog(
            initialHour = editingAlarm?.hour ?: calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = editingAlarm?.minute ?: calendar.get(Calendar.MINUTE),
            onDismissRequest = { 
                showTimePicker = false
                editingAlarm = null
            },
            onTimeSelected = { hour, minute ->
                if (editingAlarm != null) {
                    onUpdateAlarm(editingAlarm!!.id, hour, minute)
                } else {
                    onAddAlarm(hour, minute)
                }
                showTimePicker = false
                editingAlarm = null
            }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Routine Reminder", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Master", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = alarms.any { it.isActive },
                            onCheckedChange = onToggleMaster
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTimePicker = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Rounded.Add, 
                    contentDescription = "Add Alarm"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (activeAlarms.isNotEmpty()) {
                item {
                    SectionHeader("Active Alarms")
                }
                items(activeAlarms, key = { it.id }) { alarm ->
                    AlarmItemRow(
                        alarm = alarm,
                        onToggle = { onToggleAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) },
                        onClick = { editingAlarm = alarm },
                        onToggleDay = { day -> onToggleDay(alarm.id, day) }
                    )
                }
            }

            if (inactiveAlarms.isNotEmpty()) {
                item {
                    SectionHeader("Inactive Alarms")
                }
                items(inactiveAlarms, key = { it.id }) { alarm ->
                    AlarmItemRow(
                        alarm = alarm,
                        onToggle = { onToggleAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) },
                        onClick = { editingAlarm = alarm },
                        onToggleDay = { day -> onToggleDay(alarm.id, day) }
                    )
                }
            }
            
            // Padding at the bottom for FAB
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RoutineReminderScreenPreview() {
    val dummyAlarms = listOf(
        AlarmItem(1, "09:00 AM", 9, 0, isActive = true),
        AlarmItem(2, "11:00 AM", 11, 0, isActive = true),
        AlarmItem(3, "01:00 PM", 13, 0, isActive = false)
    )
    AlarmTheme {
        RoutineReminderContent(
            alarms = dummyAlarms,
            onToggleAlarm = {},
            onDeleteAlarm = {},
            onAddAlarm = { _, _ -> },
            onUpdateAlarm = { _, _, _ -> },
            onToggleMaster = {},
            onToggleDay = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun RoutineReminderScreenTabletPreview() {
    val dummyAlarms = listOf(
        AlarmItem(1, "09:00 AM", 9, 0, isActive = true),
        AlarmItem(2, "11:00 AM", 11, 0, isActive = true),
        AlarmItem(3, "01:00 PM", 13, 0, isActive = false)
    )
    AlarmTheme {
        RoutineReminderContent(
            alarms = dummyAlarms,
            onToggleAlarm = {},
            onDeleteAlarm = {},
            onAddAlarm = { _, _ -> },
            onUpdateAlarm = { _, _, _ -> },
            onToggleMaster = {},
            onToggleDay = { _, _ -> }
        )
    }
}
