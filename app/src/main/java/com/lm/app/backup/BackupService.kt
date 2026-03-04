package com.lm.app.backup

import android.content.Context
import com.lm.app.data.LeaveRepository
import com.lm.app.data.User
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    private val repository: LeaveRepository,
    private val driveService: GoogleDriveService
) {
    suspend fun performBackup(context: Context, user: User): Result<String> {
        return try {
            val balance = repository.getLeaveBalance(user.kgid)
            val entries = repository.getLeaveEntries(user.kgid).first()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            // 1. Generate and Upload Excel (Human readable)
            val excelFile = ExcelExporter.exportToExcel(context, entries, currentYear)
            driveService.uploadFile(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

            // 2. Generate and Upload JSON (Machine readable for Restore)
            val backupData = BackupData(
                exportDate = System.currentTimeMillis(),
                balance = balance,
                entries = entries
            )
            val jsonFile = JsonBackupHandler.generateJsonBackupFile(context.cacheDir, backupData)
            driveService.uploadFile(jsonFile, "application/json")

            Result.success("Backup successful")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(user: User): Boolean {
        return try {
            val jsonString = driveService.downloadJsonBackup() ?: return false
            val backupData = JsonBackupHandler.parseBackupData(jsonString)
            
            // Repopulate local database
            repository.saveLeaveBalance(backupData.balance)
            for (entry in backupData.entries) {
                repository.saveLeaveEntry(backupData.balance, entry)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
