package com.lm.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lm.app.ui.viewmodel.BackupStatus
import com.lm.app.ui.viewmodel.BackupViewModel
import com.lm.app.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val backupStatus by backupViewModel.backupStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(backupStatus) {
        when (backupStatus) {
            is BackupStatus.Success -> {
                snackbarHostState.showSnackbar("Backup successful! Saved to Google Drive.")
                backupViewModel.resetStatus()
            }
            is BackupStatus.Error -> {
                snackbarHostState.showSnackbar((backupStatus as BackupStatus.Error).message)
                backupViewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Backup to Google Drive") },
                supportingContent = { Text("Save your leave data securely to the cloud.") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Backup",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    if (backupStatus is BackupStatus.InProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                modifier = Modifier.clickable(
                    enabled = backupStatus !is BackupStatus.InProgress
                ) {
                    if (backupStatus !is BackupStatus.InProgress) {
                        currentUser?.let { backupViewModel.performBackup(context, it) }
                    }
                }
            )
        }
    }
}
