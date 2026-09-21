package com.example.alarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarm.viewmodel.BackfillViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillScreen(
    viewModel: BackfillViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val existingIntake by viewModel.existingIntake.collectAsStateWithLifecycle()
    val amountToRestore by viewModel.amountToRestore.collectAsStateWithLifecycle()
    val previewLogs by viewModel.previewLogs.collectAsStateWithLifecycle()

    var isMultipleMode by remember { mutableStateOf(false) }
    
    // Single Mode State
    var singleTime by remember { mutableStateOf(LocalTime.now()) }
    var singleAmount by remember { mutableStateOf("") }
    
    // Multiple Mode State
    var startTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(21, 0)) }
    var entryCount by remember { mutableStateOf("6") }
    var customRestoreAmount by remember { mutableStateOf(amountToRestore.toString()) }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Time Picker State (Simplified for mockup, ideally use M3 TimePicker dialogs)
    var showTimePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf("SINGLE") } // SINGLE, START, END
    val timePickerState = rememberTimePickerState()

    LaunchedEffect(amountToRestore) {
        if (customRestoreAmount.isEmpty() || customRestoreAmount.toIntOrNull() == null) {
            customRestoreAmount = amountToRestore.toString()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDate(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    when (timePickerTarget) {
                        "SINGLE" -> singleTime = selectedTime
                        "START" -> startTime = selectedTime
                        "END" -> endTime = selectedTime
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restore Missed Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true }
                ) {
                    PaddingValues(16.dp)
                    Text(
                        text = "Date: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Goal")
                            Text("$dailyGoal ml", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Already recorded")
                            Text("$existingIntake ml", fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Suggested restore")
                            Text("${maxOf(0, dailyGoal - existingIntake)} ml", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !isMultipleMode,
                        onClick = { isMultipleMode = false },
                        label = { Text("Single Entry") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isMultipleMode,
                        onClick = { isMultipleMode = true },
                        label = { Text("Multiple Entries") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!isMultipleMode) {
                item {
                    OutlinedTextField(
                        value = singleAmount,
                        onValueChange = { singleAmount = it },
                        label = { Text("Amount (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { timePickerTarget = "SINGLE"; showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Time: ${singleTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                    }
                }
                item {
                    Button(
                        onClick = {
                            val amt = singleAmount.toIntOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.addSingleEntry(amt, singleTime, "Backfilled")
                                singleAmount = "" // clear after save
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Entry")
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = customRestoreAmount,
                        onValueChange = { 
                            customRestoreAmount = it
                            val amt = it.toIntOrNull()
                            if (amt != null) viewModel.setAmountToRestore(amt)
                        },
                        label = { Text("Amount to restore (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { timePickerTarget = "START"; showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start: ${startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                        }
                        OutlinedButton(
                            onClick = { timePickerTarget = "END"; showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("End: ${endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = entryCount,
                        onValueChange = { entryCount = it },
                        label = { Text("Number of entries") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Button(
                        onClick = {
                            val count = entryCount.toIntOrNull()
                            if (count != null && count > 0) {
                                viewModel.generatePreview(startTime, endTime, count)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Preview")
                    }
                }

                if (previewLogs.isNotEmpty()) {
                    item {
                        Text("Preview", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                    }
                    items(previewLogs) { log ->
                        val timeStr = java.time.Instant.ofEpochMilli(log.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm a"))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(timeStr)
                            Text("${log.amountMl} ml", fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.savePreview() },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Text("Save All")
                        }
                    }
                }
            }
        }
    }
}
