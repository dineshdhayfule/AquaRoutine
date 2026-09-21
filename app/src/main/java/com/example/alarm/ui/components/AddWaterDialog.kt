package com.example.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddWaterDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (Int) -> Unit,
    onUnlockOwnerMode: ((String) -> Boolean)? = null,
    isPinSet: Boolean = true,
    onOpenPinSetup: (() -> Unit)? = null
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Hidden Developer Trigger State
    var tapCount by remember { mutableIntStateOf(0) }
    var showPinEntry by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (showPinEntry) {
        if (!isPinSet && onOpenPinSetup != null) {
            onOpenPinSetup()
            showPinEntry = false
            tapCount = 0
            onDismissRequest()
            return
        }
        AlertDialog(
            onDismissRequest = { 
                showPinEntry = false
                tapCount = 0
                pinError = false 
            },
            title = { Text("Owner Access") },
            text = {
                Column {
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { 
                            enteredPin = it
                            pinError = false 
                        },
                        label = { Text("Enter PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError
                    )
                    if (pinError) {
                        Text(
                            text = "Incorrect PIN",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (onUnlockOwnerMode?.invoke(enteredPin) == true) {
                        showPinEntry = false
                        pinError = false
                        onDismissRequest() // Close the add dialog too, unlocking happened
                    } else {
                        enteredPin = ""
                        pinError = true
                    }
                }) { Text("Unlock") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPinEntry = false
                    tapCount = 0
                    pinError = false 
                }) { Text("Cancel") }
            }
        )
        return // Suspend rendering the main dialog
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(
                text = "Add Water Log",
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            tapCount++
                            if (tapCount >= 3) {
                                showPinEntry = true
                            }
                        }
                    )
                }
            ) 
        },
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
