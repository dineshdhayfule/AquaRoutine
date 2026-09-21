package com.example.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinManagementDialog(
    isPinSet: Boolean,
    onVerifyPin: (String) -> Boolean,
    onSavePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    // State machine: 
    // If not set -> "CREATE"
    // If set -> "VERIFY" -> then either "CHANGE" or "DISABLE"
    var mode by remember { mutableStateOf(if (isPinSet) "VERIFY" else "CREATE") }
    var nextAction by remember { mutableStateOf("NONE") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(text = when(mode) {
                "CREATE", "CHANGE" -> "Set Owner PIN"
                "VERIFY" -> "Verify Current PIN"
                else -> "Manage Security"
            })
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (mode == "VERIFY") {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it; errorMsg = null },
                        label = { Text("Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            if (onVerifyPin(currentPin)) {
                                mode = "CHANGE"
                                errorMsg = null
                            } else {
                                errorMsg = "Incorrect PIN"
                            }
                        }) {
                            Text("Change PIN")
                        }
                        
                        TextButton(onClick = {
                            if (onVerifyPin(currentPin)) {
                                onDisablePin()
                                onDismissRequest()
                            } else {
                                errorMsg = "Incorrect PIN"
                            }
                        }) {
                            Text("Disable PIN", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else if (mode == "CREATE" || mode == "CHANGE") {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it; errorMsg = null },
                        label = { Text("New PIN (min 4 digits)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it; errorMsg = null },
                        label = { Text("Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            if (mode == "CREATE" || mode == "CHANGE") {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            errorMsg = "PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            errorMsg = "PINs do not match"
                        } else {
                            onSavePin(newPin)
                            onDismissRequest()
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
