package com.lm.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

    val groupedEntries = remember(entries) {
        entries.groupBy { it.year }
               .toSortedMap(compareByDescending { it })
    }

    var expandedYears by remember { 
        mutableStateOf(groupedEntries.keys.take(1).toSet()) 
    }

    LaunchedEffect(groupedEntries.keys) {
        if (expandedYears.isEmpty() && groupedEntries.isNotEmpty()) {
            expandedYears = setOf(groupedEntries.keys.first())
        }
    }

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
        if (groupedEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No leave records found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedEntries.forEach { (year, yearEntries) ->
                    val isExpanded = expandedYears.contains(year)
                    
                    item(key = "header_$year") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    expandedYears = if (isExpanded) expandedYears - year else expandedYears + year
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Year $year",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("${yearEntries.size} items", modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }

                    if (isExpanded) {
                        items(yearEntries, key = { it.id }) { entry ->
                            GlobalLeaveEntryCard(
                                entry = entry,
                                dateFormat = dateFormat,
                                onEdit = { showEditDialog = it },
                                onDelete = { showDeleteDialog = it }
                            )
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



@Composable
fun GlobalLeaveEntryCard(
    entry: LeaveEntry,
    dateFormat: SimpleDateFormat,
    onEdit: (LeaveEntry) -> Unit,
    onDelete: (LeaveEntry) -> Unit
) {
    val baseColor = getLeaveColor(entry.leaveType, entry.isMcl)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        entry.elEntryType == "credit" -> "Credit Added (${entry.leaveType})"
                        entry.isMcl -> "${entry.leaveType} (MCL)"
                        else -> entry.leaveType
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (entry.elEntryType == "credit") Color(0xFF43A047) else baseColor,
                    modifier = Modifier.weight(1f)
                )
                if (entry.elEntryType != "credit") {
                    Row {
                        IconButton(
                            onClick = { onEdit(entry) },
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
                            onClick = { onDelete(entry) },
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
            }

            val dateStr = if (entry.dateFrom == entry.dateTo) {
                dateFormat.format(entry.dateFrom!!)
            } else {
                "${dateFormat.format(entry.dateFrom!!)} - ${dateFormat.format(entry.dateTo!!)}"
            }
            val durationValue = if (entry.totalDays % 1.0 == 0.0) entry.totalDays.toInt().toString() else "%.1f".format(entry.totalDays)
            val durationSuffix = "day${if (entry.totalDays != 1.0) "s" else ""}"

            Text(
                text = if (entry.elEntryType == "credit") {
                    "$dateStr (+ $durationValue $durationSuffix)"
                } else {
                    "$dateStr ($durationValue $durationSuffix)"
                },
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp),
                color = if (entry.elEntryType == "credit") Color(0xFF2E7D32) else Color.Unspecified
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




