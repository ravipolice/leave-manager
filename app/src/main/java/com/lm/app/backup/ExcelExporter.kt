package com.lm.app.backup

import android.content.Context
import com.lm.app.data.LeaveEntry
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun exportToExcel(context: Context, entries: List<LeaveEntry>, year: Int): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Leave Register $year")
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)

        // Header Row
        val headerRow = sheet.createRow(0)
        val headers = listOf("Leave Type", "From Date", "To Date", "Days", "Remarks", "Created At")
        headers.forEachIndexed { index, title ->
            headerRow.createCell(index).setCellValue(title)
        }

        // Data Rows
        entries.forEachIndexed { index, entry ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(entry.leaveType)
            row.createCell(1).setCellValue(entry.dateFrom?.let { dateFormat.format(it) } ?: "-")
            row.createCell(2).setCellValue(entry.dateTo?.let { dateFormat.format(it) } ?: "-")
            row.createCell(3).setCellValue(entry.totalDays)
            row.createCell(4).setCellValue(entry.remark ?: "")
            row.createCell(5).setCellValue(dateFormat.format(entry.createdAt))
        }

        // Simple fixed column widths as autoSizeColumn crashes on Android (requires AWT)
        sheet.setColumnWidth(0, 4000)
        sheet.setColumnWidth(1, 4000)
        sheet.setColumnWidth(2, 4000)
        sheet.setColumnWidth(3, 2000)
        sheet.setColumnWidth(4, 8000)
        sheet.setColumnWidth(5, 4000)

        val fileName = "Leave_Register_$year.xlsx"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()
        return file
    }
}
