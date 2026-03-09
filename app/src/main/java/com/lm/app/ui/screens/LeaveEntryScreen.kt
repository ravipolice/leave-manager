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

    val groupedEntries = remember(entries, preselectedType) {
        entries.filter { 
            if (preselectedType == "MCL") it.isMcl || it.leaveType == "MCL"
            else it.leaveType == preselectedType && !it.isMcl
        }.groupBy { it.year }
         .toSortedMap(compareByDescending { it })
    }

    // State to track which years are expanded. Default most recent year to expanded.
    var expandedYears by remember { 
        mutableStateOf(groupedEntries.keys.take(1).toSet()) 
    }

    // Update expandedYears when groupedEntries changes (e.g. initial load)
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
        if (groupedEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No records for $preselectedType", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "History of $preselectedType",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                groupedEntries.forEach { (year, yearEntries) ->
                    val isExpanded = expandedYears.contains(year)
                    
                    // Year Header
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
                                    Text("${yearEntries.size} entries", modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }

                    if (isExpanded) {
                        items(yearEntries, key = { it.id }) { entry ->
                            LeaveEntryCard(
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
fun LeaveEntryCard(
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
                        entry.elEntryType == "credit" -> "Credit Added"
                        entry.isMcl -> "Menstrual CL"
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
                entry.dateFrom?.let { dateFormat.format(it) } ?: ""
            } else {
                "${entry.dateFrom?.let { dateFormat.format(it) } ?: ""} - ${entry.dateTo?.let { dateFormat.format(it) } ?: ""}"
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


