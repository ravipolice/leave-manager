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
    onNavigateToRules: () -> Unit,
    userViewModel: UserViewModel,
    leaveViewModel: LeaveViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val balance by leaveViewModel.balance.collectAsState()
    val statistics by leaveViewModel.statistics.collectAsState()
    val uiState by leaveViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        currentUser?.let { leaveViewModel.refreshData(it) }
    }

    var showEditBalanceDialog by remember { mutableStateOf(false) }
    var elText by remember { mutableStateOf("0") }
    var hplText by remember { mutableStateOf("0") }
    var showEditClLimitDialog by remember { mutableStateOf(false) }

    // Update the dialog texts when balance changes or dialog is about to show
    LaunchedEffect(showEditBalanceDialog, balance) {
        if (showEditBalanceDialog) {
            elText = balance?.elManualBalance?.toString()?.removeSuffix(".0") ?: "0"
            hplText = balance?.hplBalance?.toString()?.removeSuffix(".0") ?: "0"
        }
    }

    if (showEditBalanceDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditBalanceDialog = false },
            title = { Text("Edit Base Balances") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your carried-over balance (prior to this year's auto-credits):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = elText,
                        onValueChange = { elText = it },
                        label = { Text("Earned Leave (EL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hplText,
                        onValueChange = { hplText = it },
                        label = { Text("Half Pay Leave (HPL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    currentUser?.let { user ->
                        val newEl = elText.toDoubleOrNull() ?: balance?.elManualBalance ?: 0.0
                        val newHpl = hplText.toDoubleOrNull() ?: balance?.hplBalance ?: 0.0
                        leaveViewModel.updateInitialBalances(user, newEl, newHpl)
                    }
                    showEditBalanceDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBalanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditClLimitDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditClLimitDialog = false },
            title = { Text("Casual Leave Limit") },
            text = { Text("Select your annual Casual Leave limit:") },
            confirmButton = {},
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        currentUser?.let { user -> leaveViewModel.updateClLimit(user, 10) }
                        showEditClLimitDialog = false
                    }) {
                        Text("10 Days")
                    }
                    TextButton(onClick = {
                        currentUser?.let { user -> leaveViewModel.updateClLimit(user, 15) }
                        showEditClLimitDialog = false
                    }) {
                        Text("15 Days")
                    }
                }
            }
        )
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Leave Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Seed Data") },
                            leadingIcon = { Icon(Icons.Default.Science, contentDescription = null) },
                            onClick = {
                                currentUser?.let { leaveViewModel.seedSampleData(it) }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                currentUser?.let { leaveViewModel.refreshData(it) }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reports") },
                            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            onClick = {
                                onNavigateToReports()
                                showMenu = false
                            }
                        )
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
                                    balance = balance?.clRemaining?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) } ?: "-",
                                    subtitle = "of ${balance?.clAnnualLimit ?: 15} days",
                                    icon = Icons.Default.WbSunny,
                                    gradient = listOf(Color(0xFF43A047), Color(0xFF1B5E20)),
                                    onClick = onNavigateToCl,
                                    actionIcon = Icons.Default.Edit,
                                    onActionClick = { showEditClLimitDialog = true } 
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Earned Leave",
                                    balance = balance?.elBalance?.let { "%.0f".format(it) } ?: "-",
                                    subtitle = "days remaining",
                                    icon = Icons.Default.Star,
                                    gradient = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)),
                                    onClick = onNavigateToEl,
                                    actionIcon = Icons.Default.Edit,
                                    onActionClick = { showEditBalanceDialog = true }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LeaveTile(
                                    title = "Half Pay Leave",
                                    balance = balance?.hplBalance?.let { "%.0f".format(it) } ?: "-",
                                    subtitle = "days remaining",
                                    icon = Icons.Default.Schedule,
                                    gradient = listOf(Color(0xFFFB8C00), Color(0xFFE65100)),
                                    onClick = onNavigateToHpl,
                                    actionIcon = Icons.Default.Edit,
                                    onActionClick = { showEditBalanceDialog = true }
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
                                    balance = balance?.cclUsed?.let { "%.0f".format(it) } ?: "0",
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
                                        balance = "%.0f".format(othersTaken),
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
                                balance = "%.0f".format(othersTaken),
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
fun LeaveTile(title: String, balance: String, subtitle: String, icon: ImageVector, gradient: List<Color>, onClick: () -> Unit, actionIcon: ImageVector? = null, onActionClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(gradient)).padding(12.dp)) {
            // Top row: edit icon (start) + title (end)
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (actionIcon != null && onActionClick != null) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = actionIcon, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(28.dp))
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Centre-left: balance number + subtitle
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(top = 4.dp)) {
                Text(text = balance, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            // Background icon (bottom-end)
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
                        text = "New Leave Entry",
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
            val takenText = statistics?.totalTaken?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) } ?: "0"
            val remainingText = statistics?.totalRemaining?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) } ?: "0"
            OverviewStat(label = "Taken", value = takenText, icon = Icons.Default.EventBusy, color = MaterialTheme.colorScheme.error)
            OverviewStat(label = "Remaining", value = remainingText, icon = Icons.Default.EventAvailable, color = Color(0xFF43A047))
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
