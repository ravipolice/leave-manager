package com.lm.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lm.app.R
import com.lm.app.data.User
import com.lm.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRegisterClick: (String) -> Unit,
    onForgotPinClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    // AuthViewModel states
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()
    val authUser by authViewModel.currentUser.collectAsState()
    val navigateToRegister by authViewModel.navigateToRegister.collectAsState()
    
    // UI State
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var isEmailPinExpanded by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null && account.idToken != null) {
                authViewModel.clearError() 
                authViewModel.signInWithGoogle(account.idToken!!)
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            authViewModel.setError("Sign in failed: ${e.statusCode}")
        }
    }

    // Listen for successful login from AuthViewModel
    LaunchedEffect(authUser) {
        authUser?.let { user ->
            onLoginSuccess(user)
            authViewModel.clearCurrentUser() // Clear after navigation
        }
    }

    // Listen for navigation to register
    val pendingGoogleUser by authViewModel.pendingGoogleUser.collectAsState()
    LaunchedEffect(navigateToRegister) {
        if (navigateToRegister) {
            authViewModel.clearNavigateToRegister()
            onRegisterClick(pendingGoogleUser ?: "")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    tonalElevation = 2.dp,
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_premium),
                        contentDescription = "Leave Manager Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Header
                Text(
                    text = "Leave Manager",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Your personal digital leave register.\nSecure, offline, and self-managed.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                authError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Expandable Card for Email/PIN Login
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEmailPinExpanded = !isEmailPinExpanded },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD) // Light blue background
                    )
                ) {
                    Column {
                        // Button header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Login with email / phone",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isEmailPinExpanded) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (isEmailPinExpanded) "Collapse" else "Expand"
                            )
                        }

                        // Expandable content
                        AnimatedVisibility(visible = isEmailPinExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Identifier Input
                                OutlinedTextField(
                                    value = identifier,
                                    onValueChange = { identifier = it.trim() },
                                    label = { Text("Email or Mobile Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    )
                                )

                                // PIN Input
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) password = it },
                                    label = { Text("6-digit PIN") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        val icon = if (pinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                                        IconButton(onClick = { pinVisible = !pinVisible }) {
                                            Icon(icon, contentDescription = "Toggle PIN visibility")
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        if (identifier.isNotBlank()) {
                                            authViewModel.login(identifier, password)
                                        } else {
                                            authViewModel.setError("Please enter your email or mobile")
                                        }
                                    })
                                )

                                // Login Button
                                Button(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (identifier.isNotBlank()) {
                                            authViewModel.login(identifier, password)
                                        } else {
                                             authViewModel.setError("Please enter your email or mobile")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = identifier.isNotBlank() && password.length == 6
                                ) { 
                                    Text("Login") 
                                }

                                // Forgot PIN Link
                                Text(
                                    "Forgot PIN?",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onForgotPinClick() }
                                        .padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(" or ", modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // Google Sign-In/Register Button
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        authViewModel.clearError()
                        val webClientId = context.getString(R.string.default_web_client_id)
                        val signInIntent = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, com.lm.app.auth.GoogleAuthHelper.getSignInOptions(webClientId)).signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sign in with Google / Register", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
