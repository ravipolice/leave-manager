package com.lm.app.ui.components

import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.lm.app.R
import com.lm.app.ui.viewmodel.UserViewModel
import com.lm.app.backup.BackupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    userViewModel: UserViewModel,
    backupService: BackupService?,
    currentRoute: String?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val profilePhotoBitmap by userViewModel.profilePhotoBitmap.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) userViewModel.uploadProfilePhoto(bitmap, context)
        }
    }

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
                    
                    // Profile photo: from Google Drive or fallback icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoBitmap != null) {
                            Image(
                                bitmap = profilePhotoBitmap!!.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Tap to add photo",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
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
                    
                    // KGID (with copy icon)
                    Row(
                        modifier = Modifier
                            .clickable {
                                currentUser?.kgid?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "KGID copied: $it", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentUser?.kgid?.let { "KGID: $it" } ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy KGID",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 📱 Mobile Number
                    if (!currentUser?.phone.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = currentUser?.phone ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // ✉️ Email
                    if (!currentUser?.email.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
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
                    icon = Icons.Default.Backup,
                    text = "Backup to Google Drive",
                    onClick = {
                        scope.launch { 
                            drawerState.close() 
                            Toast.makeText(context, "Backing up to Google Drive...", Toast.LENGTH_SHORT).show()
                            currentUser?.let { user ->
                                val result = backupService?.performBackup(context, user)
                                if (result?.isSuccess == true) {
                                    Toast.makeText(context, "✅ Backup succeeded (AppData folder)!", Toast.LENGTH_LONG).show()
                                } else {
                                    val errorMsg = result?.exceptionOrNull()?.message ?: "Unknown error"
                                    Toast.makeText(context, "Backup failed: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "Must be logged in to backup", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                DrawerItem(
                    icon = Icons.Default.ExitToApp,
                    text = "Restore from Google Drive",
                    onClick = {
                        scope.launch { 
                            drawerState.close() 
                            Toast.makeText(context, "Restoring data from Drive...", Toast.LENGTH_SHORT).show()
                            currentUser?.let { user ->
                                val success = backupService?.restoreBackup(user) ?: false
                                if (success) {
                                    Toast.makeText(context, "✅ Data restored successfully!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Restore failed: No backup found in Drive.", Toast.LENGTH_LONG).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "Must be logged in to restore", Toast.LENGTH_SHORT).show()
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
