package com.lm.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lm.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPinScreen(
    onNavigateBack: () -> Unit,
    onPinResetSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    
    // AuthViewModel mapped states
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()
    
    // Synthetic Steps for LeaveManagerApp based on AuthViewModel API
    var resetStep by remember { mutableStateOf(0) } // 0: Email, 1: Verify OTP, 2: Reset PIN
    var resetCountdown by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot PIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            authError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            when (resetStep) {
                0 -> { // --- STEP 1: EMAIL ---
                    Text("Enter your registered email to receive an OTP.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Registered Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                authViewModel.setError("Please enter your email")
                                return@Button
                            }
                            authViewModel.sendPasswordResetOtp(email) { success ->
                                if (success) {
                                    resetStep = 1
                                    resetCountdown = 300 // 5 minutes
                                    Toast.makeText(context, "OTP Sent to $email", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Send OTP")
                        }
                    }
                }

                1 -> { // --- STEP 2: VERIFY OTP ---
                    LaunchedEffect(resetCountdown) {
                         if (resetCountdown > 0) {
                              delay(1000)
                              resetCountdown--
                         }
                    }
                    
                    Text("Enter the OTP sent to your email.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("Enter OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val minutes = resetCountdown / 60
                    val seconds = resetCountdown % 60
                    val timeDisplay = String.format("%02d:%02d", minutes, seconds)

                    if (resetCountdown > 0) {
                        Text(
                            text = "OTP expires in $timeDisplay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "OTP expired. Please request a new one.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (otp.isBlank()) {
                                authViewModel.setError("Please enter the OTP")
                                return@Button
                            }
                            // Local check - full verification is bundled with updatePinAfterOtp in AuthViewModel
                            authViewModel.clearError() 
                            resetStep = 2 // Move to Step 3 explicitly
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = resetCountdown > 0
                    ) {
                         Text("Verify OTP")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { resetStep = 0 }) {
                        Text("Request new OTP")
                    }
                }

                2 -> { // --- STEP 3: RESET PIN ---
                    Text("Create a new 6-digit PIN.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN") },
                        visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPin = !showNewPin }) {
                                Icon(
                                    imageVector = if (showNewPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showNewPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm PIN") },
                        visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                                Icon(
                                    imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPin) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (newPin != confirmPin || newPin.length < 6) {
                                authViewModel.setError("PINs must match and be at least 6 digits.")
                                return@Button
                            }
                            authViewModel.verifyOtpAndResetPin(email, otp, newPin) {
                                Toast.makeText(context, "PIN Reset Successfully!", Toast.LENGTH_LONG).show()
                                onPinResetSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Reset PIN")
                        }
                    }
                }
            }
        }
    }
}
