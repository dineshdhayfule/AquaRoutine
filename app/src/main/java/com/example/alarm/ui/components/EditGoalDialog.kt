package com.example.alarm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditGoalDialog(
    currentGoalMl: Int,
    onDismissRequest: () -> Unit,
    onSubmit: (Int, Boolean) -> Unit // goal, isGlobal
) {
    var goalText by remember { mutableStateOf(currentGoalMl.toString()) }
    var isGlobal by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Edit Daily Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        goalText = it
                        isError = it.toIntOrNull() == null && it.isNotEmpty()
                    },
                    label = { Text("Daily Goal (ml)") },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid number",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGlobal = false }
                    ) {
                        RadioButton(selected = !isGlobal, onClick = { isGlobal = false })
                        Text("Apply to Today Only", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGlobal = true }
                    ) {
                        RadioButton(selected = isGlobal, onClick = { isGlobal = true })
                        Text("Apply to All Days", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goal = goalText.toIntOrNull()
                    if (goal != null && goal > 0) {
                        onSubmit(goal, isGlobal)
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
