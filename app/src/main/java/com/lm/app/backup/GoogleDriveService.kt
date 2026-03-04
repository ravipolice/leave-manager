package com.lm.app.backup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File as JavaFile

@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APP_FOLDER_NAME = "LeaveManager"
        private const val PROFILE_PHOTO_FILENAME = "profile_photo.jpg"
    }

    private fun buildDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply { selectedAccount = account.account }
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Leave Manager").build()
    }

    /** Upload a profile photo bitmap to the user's Google Drive (LeaveManager folder) */
    suspend fun uploadProfilePhoto(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = buildDriveService() ?: throw Exception("Drive Service built as null (User not signed in?)")

            val tempFile = JavaFile(context.cacheDir, PROFILE_PHOTO_FILENAME)
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val fileMetadata = File().apply {
                name = PROFILE_PHOTO_FILENAME
                parents = listOf("appDataFolder")
            }
            val mediaContent = FileContent("image/jpeg", tempFile)

            val existingId = findFileId(PROFILE_PHOTO_FILENAME)
            val uploadedFile = if (existingId != null) {
                service.files().update(existingId, File(), mediaContent).execute()
            } else {
                service.files().create(fileMetadata, mediaContent).setFields("id").execute()
            }

            tempFile.delete()
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Download the profile photo bitmap from the user's Google Drive */
    suspend fun downloadProfilePhoto(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val service = buildDriveService() ?: return@withContext null
            val fileId = findFileId(PROFILE_PHOTO_FILENAME) ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val bytes = outputStream.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadFile(javaFile: JavaFile, mimeType: String): String? = withContext(Dispatchers.IO) {
        val service = buildDriveService() ?: return@withContext null
        val fileMetadata = File().apply {
            name = javaFile.name
            parents = listOf("appDataFolder")
        }
        val mediaContent = FileContent(mimeType, javaFile)
        val existingId = findFileId(javaFile.name)
        val uploaded = if (existingId != null) {
            service.files().update(existingId, fileMetadata, mediaContent).execute()
        } else {
            service.files().create(fileMetadata, mediaContent).setFields("id").execute()
        }
        uploaded.id
    }

    suspend fun downloadJsonBackup(): String? = withContext(Dispatchers.IO) {
        val service = buildDriveService() ?: return@withContext null
        val fileId = findFileId("leave_backup_metadata.json") ?: return@withContext null
        val outputStream = ByteArrayOutputStream()
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        outputStream.toString("UTF-8")
    }

    private fun findFileId(name: String, mimeType: String? = null): String? {
        val service = buildDriveService() ?: return null
        var query = "name = '$name' and trashed = false"
        if (mimeType != null) query += " and mimeType = '$mimeType'"

        val result = service.files().list()
            .setQ(query)
            .setSpaces("appDataFolder")
            .setFields("files(id)")
            .execute()
        return result.files?.firstOrNull()?.id
    }
}
