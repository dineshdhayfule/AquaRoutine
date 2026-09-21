package com.example.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddWaterDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Add Water Log") },
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
            Button(
                onClick = {
                    val amount = amountText.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onSubmit(amount)
                        onDismissRequest()
                    } else {
                        isError = true
                        errorMessage = if (amount != null && amount <= 0) "Amount must be greater than 0" else "Please enter a valid number"
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
