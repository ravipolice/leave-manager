package com.lm.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.lm.app.auth.GoogleAuthHelper
import com.lm.app.data.User
import com.lm.app.ui.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    userViewModel: UserViewModel,
    backupViewModel: com.lm.app.ui.viewmodel.BackupViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val backupStatus by backupViewModel.backupStatus.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                userViewModel.setUser(
                    User(
                        kgid = "PENDING", // User will need to enter KGID later or we use email
                        name = account.displayName ?: "",
                        gender = "male" // Default, user can change later
                    )
                )
                onLoginSuccess()
            }
        } catch (e: ApiException) {
            error = "Sign in failed: ${e.statusCode}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    error = null
                    val signInIntent = GoogleSignIn.getClient(context, GoogleAuthHelper.getSignInOptions()).signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                // In a real app, we'd use a Google Sign-In button asset
                Text("Sign in with Google")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    val signInIntent = GoogleSignIn.getClient(context, GoogleAuthHelper.getSignInOptions()).signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Start Self-Management")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Temporary Bypass Button for Testing
            OutlinedButton(
                onClick = {
                    userViewModel.setUser(
                        User(
                            kgid = "TEST12345",
                            name = "Test User",
                            gender = "male"
                        )
                    )
                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Magenta)
            ) {
                Text("Bypass Login (Testing Only)")
            }

            if (backupStatus is com.lm.app.ui.viewmodel.BackupStatus.InProgress) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else {
                TextButton(
                    onClick = { 
                        currentUser?.let { user ->
                            backupViewModel.performRestore(user) {
                                onLoginSuccess()
                            }
                        } ?: run {
                            error = "Please sign in first to restore your data."
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Restore Existing Data from Drive")
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
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
