package com.lm.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lm.app.data.LeaveEntry
import com.lm.app.data.User
import com.lm.app.ui.viewmodel.LeaveUiState
import com.lm.app.ui.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    ))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveForm(
    currentUser: User?,
    leaveViewModel: LeaveViewModel,
    uiState: LeaveUiState,
    initialType: String = "CL"
) {
    val context = LocalContext.current
    
    var leaveType by remember { mutableStateOf(initialType) }
    var dateFrom by remember { mutableStateOf<Date?>(null) }
    var dateTo by remember { mutableStateOf<Date?>(null) }
    var remark by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isHalfDay by remember { mutableStateOf(false) }
    var elEntryType by remember { mutableStateOf("taken") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val leaveTypes = listOf("CL", "EL", "HPL", "WO", "LWA", "ML", "PL", "CCL", "MCL")

    fun showDatePicker(initial: Date?, onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance().apply { time = initial ?: Date() }
        DatePickerDialog(context, { _, y, m, d ->
            val result = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(result.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun calculateDays(): Double {
        if (isHalfDay) return 0.5
        val from = dateFrom ?: return 0.0
        val to = dateTo ?: return 0.0
        val diff = to.time - from.time
        val calendarDays = (diff / (1000 * 60 * 60 * 24)).toDouble() + 1
        return if (leaveType == "HPL") calendarDays / 2.0 else calendarDays
    }

    val days = calculateDays()

    LaunchedEffect(uiState) {
        if (uiState is LeaveUiState.Success) {
            leaveViewModel.resetUiState()
            dateFrom = null
            dateTo = null
            remark = ""
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Apply for Leave", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = leaveType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Leave Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    leaveTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                leaveType = type
                                expanded = false
                                isHalfDay = false
                                elEntryType = "taken"
                            }
                        )
                    }
                }
            }

            if (leaveType == "EL") {
                Text("EL Entry Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("taken" to "Taken", "upcoming" to "Upcoming").forEach { (value, label) ->
                        FilterChip(
                            selected = elEntryType == value,
                            onClick = { elEntryType = value },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (leaveType == "CL") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHalfDay, onCheckedChange = {
                        isHalfDay = it
                        if (it) dateTo = dateFrom
                    })
                    Text("Half Day (0.5 days)")
                }
            }

            val showToDate = !isHalfDay && leaveType !in listOf("WO", "MCL")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Wrapping in Box to ensure click is captured reliably
                Box(modifier = if (showToDate) Modifier.weight(1f) else Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFrom?.let { dateFormat.format(it) } ?: "Tap to select",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isHalfDay) "Date" else "From Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .noRippleClickable {
                                showDatePicker(dateFrom) {
                                    dateFrom = it
                                    if (isHalfDay || leaveType in listOf("WO", "MCL")) dateTo = it
                                }
                            }
                    )
                }

                if (showToDate) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = dateTo?.let { dateFormat.format(it) } ?: "Tap to select",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .noRippleClickable {
                                    showDatePicker(dateTo ?: dateFrom) { dateTo = it }
                                }
                        )
                    }
                }
            }

            if (days > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    val durationText = if (days % 1.0 == 0.0) days.toInt().toString() else "%.1f".format(days)
                    Text(
                        text = "Duration: $durationText day${if (days != 1.0) "s" else ""}",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("Remark (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (uiState is LeaveUiState.Error) {
                Text(text = (uiState as LeaveUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 4.dp))
            }

            Button(
                onClick = {
                    val user = currentUser ?: return@Button
                    val from = dateFrom ?: return@Button
                    val to = if (isHalfDay || leaveType in listOf("WO", "MCL")) from else (dateTo ?: return@Button)
                    val cal = Calendar.getInstance().apply { time = from }
                    val entry = LeaveEntry(
                        kgid = user.kgid,
                        dateFrom = from,
                        dateTo = to,
                        totalDays = days,
                        leaveType = if (leaveType == "MCL") "CL" else leaveType,
                        remark = remark.ifBlank { null },
                        isHalfDay = isHalfDay,
                        isMcl = leaveType == "MCL",
                        elEntryType = if (leaveType == "EL") elEntryType else "taken",
                        year = cal.get(Calendar.YEAR),
                        month = cal.get(Calendar.MONTH) + 1
                    )
                    leaveViewModel.applyLeave(user, entry)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = dateFrom != null && (isHalfDay || leaveType in listOf("WO", "MCL") || dateTo != null) && uiState !is LeaveUiState.Loading
            ) {
                if (uiState is LeaveUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Leave Entry")
                }
            }
        }
    }
}
