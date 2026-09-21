package com.example.alarm.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alarm.ui.components.EditGoalDialog
import com.example.alarm.ui.components.EditProfileDialog
import com.example.alarm.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val dailyGoal by viewModel.dailyGoalMl.collectAsState()
    val weight by viewModel.userWeightKg.collectAsState()
    val activityLevel by viewModel.activityLevel.collectAsState()
    val snoozeDuration by viewModel.snoozeDurationMin.collectAsState()
    val alarmSoundName by viewModel.alarmSoundName.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(uri) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val ringtone = uri?.let { RingtoneManager.getRingtone(context, it) }
            val name = ringtone?.getTitle(context) ?: "Default"
            viewModel.updateAlarmSound(uri?.toString(), name)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data") },
            text = { Text("Are you sure you want to delete all water logs and alarms? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditGoalDialog) {
        EditGoalDialog(
            currentGoalMl = dailyGoal,
            onDismissRequest = { showEditGoalDialog = false },
            onSubmit = { goal, _ -> viewModel.updateDailyGoal(goal) }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentWeight = weight,
            currentActivity = activityLevel,
            onDismissRequest = { showEditProfileDialog = false },
            onSubmit = { weight, activity -> viewModel.updateUserProfile(weight, activity) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Hydration") {
                SettingsItem(
                    icon = Icons.Rounded.WaterDrop,
                    title = "Daily Goal",
                    subtitle = "$dailyGoal ml",
                    onClick = { showEditGoalDialog = true }
                )
                SettingsItem(
                    icon = Icons.Rounded.Person,
                    title = "Profile",
                    subtitle = "Weight: $weight kg, Activity: $activityLevel",
                    onClick = { showEditProfileDialog = true }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "System") {
                SnoozeDurationSelector(
                    currentDuration = snoozeDuration,
                    onDurationSelected = { viewModel.updateSnoozeDuration(it) }
                )
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notification Sound",
                    subtitle = alarmSoundName,
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        }
                        ringtonePickerLauncher.launch(intent)
                    }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Data Management") {
                SettingsItem(
                    icon = Icons.Rounded.Upload,
                    title = "Export Data",
                    subtitle = "Save your data to a JSON file",
                    onClick = { exportLauncher.launch("hydrate_backup.json") }
                )
                SettingsItem(
                    icon = Icons.Rounded.Download,
                    title = "Import Data",
                    subtitle = "Restore your data from a JSON file",
                    onClick = { importLauncher.launch(arrayOf("application/json")) }
                )
                SettingsItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Delete All Data",
                    subtitle = "Remove all logs and alarms",
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SnoozeDurationSelector(
    currentDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Snooze,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Snooze Duration",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val durations = listOf(5, 10, 15, 30)
            durations.forEach { duration ->
                FilterChip(
                    selected = currentDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    label = { Text("$duration min") }
                )
            }
        }
    }
}
