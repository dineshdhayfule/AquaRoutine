package com.example.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentWeight: Float,
    currentActivity: String,
    onDismissRequest: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var weightText by remember { mutableStateOf(currentWeight.toString()) }
    var activityLevel by remember { mutableStateOf(currentActivity) }
    var isError by remember { mutableStateOf(false) }
    
    val activityLevels = listOf("Sedentary", "Active", "Very Active")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        isError = it.toFloatOrNull() == null && it.isNotEmpty()
                    },
                    label = { Text("Weight (kg)") },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = activityLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activity Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        activityLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    activityLevel = level
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                if (isError) {
                    Text(
                        text = "Please enter a valid weight",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightText.toFloatOrNull()
                    if (weight != null && weight > 0) {
                        onSubmit(weight, activityLevel)
                        onDismissRequest()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
