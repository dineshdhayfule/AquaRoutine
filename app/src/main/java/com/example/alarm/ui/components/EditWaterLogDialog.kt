package com.example.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.alarm.data.entity.WaterLogEntity

@Composable
fun EditWaterLogDialog(
    log: WaterLogEntity,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf(log.amountMl.toString()) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Edit Water Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        val amount = it.toIntOrNull()
                        isError = (amount == null && it.isNotEmpty()) || (amount != null && amount <= 0)
                        errorMessage = if (amount != null && amount <= 0) "Amount must be greater than 0" else "Please enter a valid number"
                    },
                    label = { Text("Amount (ml)") },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
                
                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toIntOrNull()
                            if (amount != null && amount > 0) {
                                onUpdate(amount)
                                onDismissRequest()
                            } else {
                                isError = true
                                errorMessage = if (amount != null && amount <= 0) "Amount must be greater than 0" else "Please enter a valid number"
                            }
                        }
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    )
}
