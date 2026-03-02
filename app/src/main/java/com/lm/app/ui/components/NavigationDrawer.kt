package com.lm.app.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lm.app.R // Replace with appropriate R
import com.lm.app.ui.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    userViewModel: UserViewModel,
    currentRoute: String?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedUser ->
                userViewModel.setUser(updatedUser)
                showEditProfileDialog = false
            }
        )
    }

    ModalDrawerSheet(
        modifier = Modifier
            .width(280.dp),
        drawerShape = RectangleShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {

            // ============================================================
            // 🔹 TOP SECTION: PROFILE CARD
            // ============================================================
            Surface(
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditProfileDialog = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 26.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Box placeholder for profile image for now since we may not have coil/photo urls yet
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        // Blood group placeholder (hardcoded or removed if not in user model)
                        // If it's not in LeaveManager's User model, we won't show it here right now.
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Name (bold, prominent) + rank (smaller, no brackets)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        // Current User model in LeaveManager may lack displayRank or similar
                        // Just a placeholder if we want to show gender/dob/etc
                        Text(
                            text = currentUser?.gender?.replaceFirstChar { it.uppercase() } ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        )
                    }
                    
                    // KGID (larger)
                    Text(
                        text = currentUser?.kgid?.let { "KGID: $it" } ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 16.sp
                        )
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp)

            // ============================================================
            // 🔹 MIDDLE SECTION: MENU ITEMS
            // ============================================================
            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                // Adjust Routes as they exist in LeaveManagerApp
                DrawerItem(
                    icon = Icons.Default.EventNote,
                    text = "Leave Dashboard",
                    selected = currentRoute == "dashboard",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("dashboard") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("dashboard") { inclusive = false }
                            }
                        }
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Info,
                    text = "Leave Rules",
                    selected = currentRoute == "rules",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("rules") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Settings,
                    text = "Settings",
                    selected = currentRoute == "settings",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("settings") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            HorizontalDivider(thickness = 1.dp)

            // ============================================================
            // 🔹 BOTTOM SECTION: LOGOUT + CONTACT
            // ============================================================
            Column(modifier = Modifier.padding(16.dp)) {
                var showLogoutDialog by remember { mutableStateOf(false) }
                var isLoggingOut by remember { mutableStateOf(false) }
                var showSupportDialog by remember { mutableStateOf(false) }
                val clipboardManager = LocalClipboardManager.current

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { if (!isLoggingOut) showLogoutDialog = false },
                        confirmButton = {
                            TextButton(
                                enabled = !isLoggingOut,
                                onClick = {
                                    isLoggingOut = true
                                    scope.launch {
                                        drawerState.close()
                                        onLogout()
                                        isLoggingOut = false
                                        showLogoutDialog = false
                                    }
                                }
                            ) {
                                if (isLoggingOut) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Logout", color = Color.Red)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !isLoggingOut,
                                onClick = { showLogoutDialog = false }
                            ) { Text("Cancel") }
                        },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        title = { Text("Confirm Logout") },
                        text = { Text("Are you sure you want to log out?") }
                    )
                }

                if (showSupportDialog) {
                    AlertDialog(
                        onDismissRequest = { showSupportDialog = false },
                        icon = { Icon(Icons.Default.Email, contentDescription = null) },
                        title = { Text("Contact Support") },
                        text = { Text("Email: support@example.com\nWe usually respond quickly.") },
                        confirmButton = {
                            TextButton(onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@example.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Leave App Support Request")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                                showSupportDialog = false
                            }) {
                                Text("Email")
                            }
                        },
                        dismissButton = {
                            Row {
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString("support@example.com"))
                                    Toast.makeText(context, "Email copied", Toast.LENGTH_SHORT).show()
                                }) { Text("Copy") }
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(onClick = { showSupportDialog = false }) { Text("Close") }
                            }
                        }
                    )
                }

                DrawerItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    text = "Logout",
                    textColor = Color.Red,
                    onClick = { showLogoutDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DrawerItem(
                    icon = Icons.Default.Email,
                    text = "Contact Support",
                    onClick = { showSupportDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================
// 🔹 Reusable Drawer Item Composable
// ============================================================
@Composable
fun DrawerItem(
    icon: ImageVector,
    text: String,
    selected: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val containerColor = if (selected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = textColor
            )
        )
    }
}
