package com.lm.app.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object JsonBackupHandler {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun generateJsonBackupFile(cacheDir: File, data: BackupData): File {
        val file = File(cacheDir, "leave_backup_metadata.json")
        val jsonString = json.encodeToString(data)
        file.writeText(jsonString)
        return file
    }

    fun parseBackupData(jsonString: String): BackupData {
        return json.decodeFromString(jsonString)
    }
}
