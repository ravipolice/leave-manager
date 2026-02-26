package com.lm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lm.app.ui.components.ApplyLeaveForm
import com.lm.app.ui.viewmodel.LeaveUiState
import com.lm.app.ui.viewmodel.LeaveViewModel
import com.lm.app.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    leaveViewModel: LeaveViewModel,
    initialType: String = "CL"
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val uiState by leaveViewModel.uiState.collectAsState()

    // Reset form state when leave is successfully applied
    LaunchedEffect(uiState) {
        if (uiState is LeaveUiState.Success) {
            // Optional: Show a message or navigate back
            // onNavigateBack() // Uncomment if you want to auto-close after success
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ApplyLeaveForm(
                currentUser = currentUser,
                leaveViewModel = leaveViewModel,
                uiState = uiState,
                initialType = initialType
            )
        }
    }
}
