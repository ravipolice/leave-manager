package com.lm.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lm.app.data.LeaveEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLeaveDialog(
    entry: LeaveEntry,
    onDismiss: () -> Unit,
    onSave: (LeaveEntry) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    var dateFrom by remember { mutableStateOf(entry.dateFrom) }
    var dateTo by remember { mutableStateOf(entry.dateTo) }
    var remark by remember { mutableStateOf(entry.remark ?: "") }
    var isHalfDay by remember { mutableStateOf(entry.isHalfDay) }
    var elEntryType by remember { mutableStateOf(entry.elEntryType) }

    val leaveType = entry.leaveType
    val showToDate = !isHalfDay && leaveType !in listOf("WO", "MCL")

    fun showDatePicker(initial: Date?, onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance().apply { time = initial ?: Date() }
        DatePickerDialog(context, { _, y, m, d ->
            val result = Calendar.getInstance().apply {
                set(y, m, d, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(result.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun calculateDays(): Double {
        if (isHalfDay) return 0.5
        val from = dateFrom ?: return entry.totalDays
        val to = dateTo ?: return entry.totalDays
        val diff = to.time - from.time
        val calendarDays = (diff / (1000 * 60 * 60 * 24)).toDouble() + 1
        return if (leaveType == "HPL") calendarDays / 2.0 else calendarDays
    }

    val days = calculateDays()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ${if (entry.isMcl) "Menstrual CL" else leaveType} Entry",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Half-day toggle for CL
                if (leaveType == "CL" && !entry.isMcl) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isHalfDay, onCheckedChange = {
                            isHalfDay = it
                            if (it) dateTo = dateFrom
                        })
                        Text("Half Day")
                    }
                }

                // EL entry type
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

                // Date pickers
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = if (showToDate) Modifier.weight(1f) else Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dateFrom?.let { dateFormat.format(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isHalfDay) "Date" else "From") },
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
                                value = dateTo?.let { dateFormat.format(it) } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To") },
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

                // Duration preview
                if (days > 0) {
                    val daysStr = if (days % 1.0 == 0.0) days.toInt().toString() else "%.1f".format(days)
                    Text(
                        text = "Duration: $daysStr day${if (days != 1.0) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Remark
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val from = dateFrom ?: return@TextButton
                    val to = if (isHalfDay || leaveType in listOf("WO", "MCL")) from else (dateTo ?: return@TextButton)
                    val cal = Calendar.getInstance().apply { time = from }
                    val updatedEntry = entry.copy(
                        dateFrom = from,
                        dateTo = to,
                        totalDays = days,
                        remark = remark.ifBlank { null },
                        isHalfDay = isHalfDay,
                        elEntryType = if (leaveType == "EL") elEntryType else entry.elEntryType,
                        year = cal.get(Calendar.YEAR),
                        month = cal.get(Calendar.MONTH) + 1,
                        modifiedAt = java.util.Date()
                    )
                    onSave(updatedEntry)
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
