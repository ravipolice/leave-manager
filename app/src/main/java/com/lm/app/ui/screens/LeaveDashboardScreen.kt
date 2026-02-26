package com.lm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lm.app.data.LeaveBalance
import com.lm.app.data.LeaveStatistics
import com.lm.app.ui.viewmodel.BackupStatus
import com.lm.app.ui.viewmodel.BackupViewModel
import com.lm.app.ui.viewmodel.LeaveUiState
import com.lm.app.ui.viewmodel.LeaveViewModel
import com.lm.app.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveDashboardScreen(
    onNavigateToCl: () -> Unit,
    onNavigateToEl: () -> Unit,
    onNavigateToHpl: () -> Unit,
    onNavigateToWo: () -> Unit,
    onNavigateToCcl: () -> Unit,
    onNavigateToMcl: () -> Unit,
    onNavigateToOther: () -> Unit,
    onNavigateToApplyLeave: () -> Unit,
    onNavigateToReports: () -> Unit,
    userViewModel: UserViewModel,
    leaveViewModel: LeaveViewModel,
    backupViewModel: BackupViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val balance by leaveViewModel.balance.collectAsState()
    val statistics by leaveViewModel.statistics.collectAsState()
    val uiState by leaveViewModel.uiState.collectAsState()
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

    LaunchedEffect(currentUser) {
        currentUser?.let { leaveViewModel.refreshData(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Leave Manager", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { currentUser?.let { leaveViewModel.seedSampleData(it) } }) {
                        Icon(Icons.Default.Science, contentDescription = "Seed Data")
                    }
                    IconButton(onClick = { currentUser?.let { leaveViewModel.refreshData(it) } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = { currentUser?.let { backupViewModel.performBackup(context, it) } },
                        enabled = backupStatus !is BackupStatus.InProgress
                    ) {
                        if (backupStatus is BackupStatus.InProgress) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Backup Now")
                        }
                    }
                    IconButton(onClick = onNavigateToReports) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reports")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState is LeaveUiState.Loading && balance == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState is LeaveUiState.Error && balance == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text((uiState as LeaveUiState.Error).message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { currentUser?.let { leaveViewModel.refreshData(it) } }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LeaveOverviewCard(balance = balance, statistics = statistics)

                    // Leave Tiles Manual Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Casual Leave",
                                    balance = balance?.clRemaining?.let { "%.1f".format(it) } ?: "-",
                                    subtitle = "of ${balance?.clAnnualLimit ?: 15} days",
                                    icon = Icons.Default.WbSunny,
                                    gradient = listOf(Color(0xFF43A047), Color(0xFF1B5E20)),
                                    onClick = onNavigateToCl
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Earned Leave",
                                    balance = balance?.elBalance?.let { "%.1f".format(it) } ?: "-",
                                    subtitle = "days remaining",
                                    icon = Icons.Default.Star,
                                    gradient = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)),
                                    onClick = onNavigateToEl
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Half Pay Leave",
                                    balance = balance?.hplBalance?.let { "%.1f".format(it) } ?: "-",
                                    subtitle = "days remaining",
                                    icon = Icons.Default.Schedule,
                                    gradient = listOf(Color(0xFFFB8C00), Color(0xFFE65100)),
                                    onClick = onNavigateToHpl
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                val woTaken = statistics?.leaveTypeBreakdown?.get("WO") ?: 0.0
                                LeaveTile(
                                    title = "Weekly Off",
                                    balance = "%.0f".format(woTaken),
                                    subtitle = "this year",
                                    icon = Icons.Default.Weekend,
                                    gradient = listOf(Color(0xFF8E24AA), Color(0xFF4A148C)),
                                    onClick = onNavigateToWo
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Child Care Leave",
                                    balance = balance?.cclUsed?.let { "%.1f".format(it) } ?: "0.0",
                                    subtitle = "days used",
                                    icon = Icons.Default.ChildCare,
                                    gradient = listOf(Color(0xFF00ACC1), Color(0xFF006064)),
                                    onClick = onNavigateToCcl
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (currentUser?.gender?.lowercase() == "female") {
                                    val cal = java.util.Calendar.getInstance()
                                    val usedThisMonth = balance?.mclLastUsedMonth == cal.get(java.util.Calendar.MONTH) + 1
                                            && balance?.mclLastUsedYear == cal.get(java.util.Calendar.YEAR)
                                    LeaveTile(
                                        title = "Menstrual CL",
                                        balance = if (usedThisMonth) "Used" else "Available",
                                        subtitle = "this month",
                                        icon = Icons.Default.Favorite,
                                        gradient = listOf(Color(0xFFE91E63), Color(0xFF880E4F)),
                                        onClick = onNavigateToMcl
                                    )
                                } else {
                                    val othersTaken = (statistics?.leaveTypeBreakdown?.get("ML") ?: 0.0) +
                                            (statistics?.leaveTypeBreakdown?.get("PL") ?: 0.0) +
                                            (statistics?.leaveTypeBreakdown?.get("LWA") ?: 0.0)
                                    LeaveTile(
                                        title = "Others / LWA",
                                        balance = "%.1f".format(othersTaken),
                                        subtitle = "days this year",
                                        icon = Icons.Default.MoreHoriz,
                                        gradient = listOf(Color(0xFF546E7A), Color(0xFF263238)),
                                        onClick = onNavigateToOther
                                    )
                                }
                            }
                        }
                        
                        if (currentUser?.gender?.lowercase() == "female") {
                            val othersTaken = (statistics?.leaveTypeBreakdown?.get("ML") ?: 0.0) +
                                    (statistics?.leaveTypeBreakdown?.get("PL") ?: 0.0) +
                                    (statistics?.leaveTypeBreakdown?.get("LWA") ?: 0.0)
                             LeaveTile(
                                title = "Others / LWA",
                                balance = "%.1f".format(othersTaken),
                                subtitle = "days this year",
                                icon = Icons.Default.MoreHoriz,
                                gradient = listOf(Color(0xFF546E7A), Color(0xFF263238)),
                                onClick = onNavigateToOther
                            )
                        }

                        // Full width Apply for Leave tile
                        ApplyTile(onClick = onNavigateToApplyLeave)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState is LeaveUiState.Loading && balance != null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun LeaveTile(title: String, balance: String, subtitle: String, icon: ImageVector, gradient: List<Color>, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(gradient)).padding(16.dp)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd))
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(text = balance, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp).align(Alignment.BottomEnd))
        }
    }
}

@Composable
fun ApplyTile(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Apply for Leave",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unified application form",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun LeaveOverviewCard(balance: LeaveBalance?, statistics: LeaveStatistics?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            OverviewStat(label = "Taken", value = statistics?.totalTaken?.let { "%.1f".format(it) } ?: "0.0", icon = Icons.Default.EventBusy, color = MaterialTheme.colorScheme.error)
            OverviewStat(label = "Remaining", value = statistics?.totalRemaining?.let { "%.1f".format(it) } ?: "0.0", icon = Icons.Default.EventAvailable, color = Color(0xFF43A047))
            OverviewStat(label = "Most Used", value = statistics?.mostUsedType?.ifEmpty { "-" } ?: "-", icon = Icons.Default.TrendingUp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun OverviewStat(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
