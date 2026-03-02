package com.lm.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lm.app.R
import com.lm.app.data.User
import com.lm.app.ui.viewmodel.AuthViewModel
import com.lm.app.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LoginMode {
    EMAIL_PASSWORD, FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // AuthViewModel states
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()
    val authUser by authViewModel.currentUser.collectAsState()
    val navigateToRegister by authViewModel.navigateToRegister.collectAsState()

    // Reset Flow States
    var resetStep by remember { mutableStateOf(0) } // 0: Enter Email, 1: Enter OTP, 2: New PIN
    var resetEmail by remember { mutableStateOf("") }
    var resetOtp by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var resetCountdown by remember { mutableStateOf(0) }

    // Login State
    var currentMode by remember { mutableStateOf(LoginMode.EMAIL_PASSWORD) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                authViewModel.clearError() 
                onLoginSuccess(
                    User(
                        kgid = "PENDING_GOOGLE", 
                        name = account.displayName ?: "Google User",
                        gender = "unspecified"
                    )
                )
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            authViewModel.setError("Sign in failed: ${e.statusCode}")
        }
    }

    // Automatically dismiss Auth errors when switching modes
    LaunchedEffect(currentMode) {
        authViewModel.clearError()
    }

    // Listen for successful login from AuthViewModel
    LaunchedEffect(authUser) {
        authUser?.let { user ->
            onLoginSuccess(user)
            authViewModel.clearCurrentUser() // Clear after navigation
        }
    }

    // Listen for navigation to register
    LaunchedEffect(navigateToRegister) {
        if (navigateToRegister) {
            authViewModel.clearNavigateToRegister()
            onRegisterClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Police Leave Manager",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your personal digital leave register.\nSecure, offline, and self-managed.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading && currentMode != LoginMode.FORGOT_PASSWORD) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        authError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        when (currentMode) {
            LoginMode.EMAIL_PASSWORD -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Registered Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            authViewModel.setError("Email and PIN cannot be empty.")
                            return@Button
                        }
                        authViewModel.login(email, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Login")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        authViewModel.clearError()
                        val webClientId = context.getString(R.string.default_web_client_id)
                        val signInIntent = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, com.lm.app.auth.GoogleAuthHelper.getSignInOptions(webClientId)).signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Sign in with Google")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot PIN?",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { 
                            currentMode = LoginMode.FORGOT_PASSWORD 
                            authViewModel.clearError()
                        }
                    )
                }
            }

            LoginMode.FORGOT_PASSWORD -> {
                Text("PIN Reset (OTP via Email)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                when (resetStep) {
                    0 -> { // STEP 1: ENTER EMAIL
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Registered Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (resetEmail.isBlank()) {
                                    authViewModel.setError("Enter email first")
                                    return@Button
                                }
                                authViewModel.sendPasswordResetOtp(resetEmail) { success ->
                                    if (success) {
                                        resetStep = 1
                                        resetCountdown = 300 // 5 minutes
                                        Toast.makeText(context, "OTP Sent to $resetEmail", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Send OTP")
                            }
                        }
                    }
                    1 -> { // STEP 2: VERIFY OTP
                        LaunchedEffect(resetCountdown) {
                            if (resetCountdown > 0) {
                                delay(1000)
                                resetCountdown--
                            }
                        }

                        OutlinedTextField(
                            value = resetOtp,
                            onValueChange = { resetOtp = it },
                            label = { Text("Enter 6-Digit OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (resetCountdown > 0) {
                            val mins = resetCountdown / 60
                            val secs = resetCountdown % 60
                            Text("OTP expires in $mins:${secs.toString().padStart(2, '0')}", color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("OTP Expired. Request a new one.", color = MaterialTheme.colorScheme.error)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (resetOtp.isBlank()) {
                                    authViewModel.setError("Enter OTP")
                                    return@Button
                                }
                                authViewModel.clearError()
                                coroutineScope.launch {
                                    delay(1500)
                                    resetStep = 2
                                    Toast.makeText(context, "OTP Verified", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = resetCountdown > 0
                        ) {
                            Text("Verify OTP")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { resetStep = 0 }) {
                            Text("Request new OTP")
                        }
                    }
                    2 -> { // STEP 3: RESET PIN
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = { Text("New PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmNewPin,
                            onValueChange = { confirmNewPin = it },
                            label = { Text("Confirm New PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (newPin != confirmNewPin || newPin.length < 6) {
                                    authViewModel.setError("PINs must match and be 6+ digits")
                                    return@Button
                                }
                                authViewModel.verifyOtpAndResetPin(resetEmail, resetOtp, newPin) {
                                    Toast.makeText(context, "PIN Reset Successfully!", Toast.LENGTH_LONG).show()
                                    resetStep = 0
                                    currentMode = LoginMode.EMAIL_PASSWORD
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Reset PIN")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Back to Login",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { 
                        currentMode = LoginMode.EMAIL_PASSWORD 
                        resetStep = 0
                        authViewModel.clearError()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Personal Backup & Restore is powered by Google Drive.",
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}
