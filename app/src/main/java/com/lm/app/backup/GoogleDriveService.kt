package com.lm.app.backup

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File as JavaFile

@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val driveService: Drive? by lazy {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@lazy null
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = account.account

        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Police Leave Manager").build()
    }

    suspend fun uploadFile(javaFile: JavaFile, mimeType: String, folderId: String? = null): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext null

        val fileMetadata = File().apply {
            name = javaFile.name
            if (folderId != null) {
                parents = listOf(folderId)
            }
        }

        val mediaContent = FileContent(mimeType, javaFile)
        
        // Check if file already exists to overwrite
        val existingFileId = findFileId(javaFile.name, folderId)
        
        val uploadedFile = if (existingFileId != null) {
            service.files().update(existingFileId, fileMetadata, mediaContent).execute()
        } else {
            service.files().create(fileMetadata, mediaContent).execute()
        }
        
        uploadedFile.id
    }

    suspend fun getOrCreateFolder(folderName: String): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext null
        
        val existingFolderId = findFileId(folderName, null, "application/vnd.google-apps.folder")
        if (existingFolderId != null) return@withContext existingFolderId

        val folderMetadata = File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
        }

        val folder = service.files().create(folderMetadata).setFields("id").execute()
        folder.id
    }

    private fun findFileId(name: String, parentId: String?, mimeType: String? = null): String? {
        val service = driveService ?: return null
        var query = "name = '$name' and trashed = false"
        if (parentId != null) query += " and '$parentId' in parents"
        if (mimeType != null) query += " and mimeType = '$mimeType'"
        
        val result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()
        
        return result.files?.firstOrNull()?.id
    }

    suspend fun downloadJsonBackup(folderName: String): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext null
        val folderId = getOrCreateFolder(folderName) ?: return@withContext null
        val fileId = findFileId("leave_backup_metadata.json", folderId) ?: return@withContext null
        
        val outputStream = java.io.ByteArrayOutputStream()
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        outputStream.toString("UTF-8")
    }
}
