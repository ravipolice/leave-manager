package com.lm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lm.app.data.LeaveEntry
import com.lm.app.ui.components.EditLeaveDialog
import com.lm.app.ui.viewmodel.LeaveViewModel
import com.lm.app.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveEntryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApplyLeave: (String) -> Unit,
    preselectedType: String = "CL",
    userViewModel: UserViewModel,
    leaveViewModel: LeaveViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val entries by leaveViewModel.entries.collectAsState()
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    val filteredEntries = remember(entries, preselectedType) {
        entries.filter { 
            if (preselectedType == "MCL") it.isMcl || it.leaveType == "MCL"
            else it.leaveType == preselectedType && !it.isMcl
        }
    }

    var showDeleteDialog by remember { mutableStateOf<LeaveEntry?>(null) }
    var showEditDialog by remember { mutableStateOf<LeaveEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$preselectedType History", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToApplyLeave(preselectedType) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Leave")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // History Section
            Text(text = "Previous $preselectedType Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("No records for $preselectedType", color = Color.Gray)
                }
            } else {
                filteredEntries.forEach { entry ->
                    val baseColor = getLeaveColor(entry.leaveType, entry.isMcl)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (entry.isMcl) "Menstrual CL" else entry.leaveType,
                                    fontWeight = FontWeight.Bold,
                                    color = baseColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(
                                        onClick = { showEditDialog = entry },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(18.dp),
                                            tint = baseColor
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDeleteDialog = entry },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            
                            val dateStr = if (entry.dateFrom == entry.dateTo) {
                                entry.dateFrom?.let { dateFormat.format(it) } ?: ""
                            } else {
                                "${entry.dateFrom?.let { dateFormat.format(it) } ?: ""} - ${entry.dateTo?.let { dateFormat.format(it) } ?: ""}"
                            }
                            val durationValue = if (entry.totalDays % 1.0 == 0.0) entry.totalDays.toInt().toString() else "%.1f".format(entry.totalDays)
                            val durationSuffix = "day${if (entry.totalDays != 1.0) "s" else ""}"
                            
                            Text(
                                text = "$dateStr ($durationValue $durationSuffix)",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            
                            entry.remark?.let {
                                Text(
                                    text = "Note: $it",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this leave entry? This will restore your leave balance.") },
            confirmButton = {
                TextButton(onClick = {
                    val entry = showDeleteDialog!!
                    val user = currentUser ?: return@TextButton
                    leaveViewModel.deleteLeaveEntry(user, entry)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    showEditDialog?.let { entryToEdit ->
        EditLeaveDialog(
            entry = entryToEdit,
            onDismiss = { showEditDialog = null },
            onSave = { updatedEntry ->
                val user = currentUser ?: return@EditLeaveDialog
                leaveViewModel.updateLeaveEntry(user, entryToEdit, updatedEntry)
                showEditDialog = null
            }
        )
    }
}
