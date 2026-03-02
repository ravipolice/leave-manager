package com.lm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun LeaveHistoryScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    leaveViewModel: LeaveViewModel
) {
    val entries by leaveViewModel.entries.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    var showDeleteDialog by remember { mutableStateOf<LeaveEntry?>(null) }
    var showEditDialog by remember { mutableStateOf<LeaveEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave History") },
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
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No leave records found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
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
                                    text = "${entry.leaveType}${if(entry.isMcl) " (MCL)" else ""}",
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
                                dateFormat.format(entry.dateFrom!!)
                            } else {
                                "${dateFormat.format(entry.dateFrom!!)} - ${dateFormat.format(entry.dateTo!!)}"
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

fun getLeaveColor(leaveType: String, isMcl: Boolean): Color {
    return when {
        isMcl -> Color(0xFFE91E63)
        leaveType == "CL" -> Color(0xFF43A047)
        leaveType == "EL" -> Color(0xFF1E88E5)
        leaveType == "HPL" -> Color(0xFFFB8C00)
        leaveType == "WO" -> Color(0xFF8E24AA)
        leaveType == "CCL" -> Color(0xFF00ACC1)
        else -> Color(0xFF546E7A)
    }
}
