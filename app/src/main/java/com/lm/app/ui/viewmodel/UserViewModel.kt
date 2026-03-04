package com.lm.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.ByteArrayContent
import com.lm.app.backup.GoogleDriveService
import com.lm.app.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val driveService: GoogleDriveService
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _profilePhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val profilePhotoBitmap = _profilePhotoBitmap.asStateFlow()

    fun setUser(user: User) {
        _currentUser.value = user
        // Load profile photo from Drive when user is set
        viewModelScope.launch {
            _profilePhotoBitmap.value = driveService.downloadProfilePhoto()
        }
    }

    fun logout(context: Context) {
        _currentUser.value = null
        _profilePhotoBitmap.value = null
        
        // Sign out of Google to clear cached account, forcing the account picker to show next time
        val gso = com.lm.app.auth.GoogleAuthHelper.getSignInOptions(
            context.getString(com.lm.app.R.string.default_web_client_id)
        )
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut()
        googleSignInClient.revokeAccess() // <--- Forces Google to forget all previously granted permissions too!
    }

    fun uploadProfilePhoto(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            val result = driveService.uploadProfilePhoto(bitmap)
            withContext(Dispatchers.Main) {
                result.onSuccess {
                    _profilePhotoBitmap.value = bitmap
                    Toast.makeText(context, "✅ Profile photo saved to Drive!", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Failed to upload photo: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun backupToGoogleDrive(context: Context) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                        ?: throw Exception("Not signed in with Google")

                    val credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_FILE)
                    ).apply { selectedAccount = account.account }

                    val drive = Drive.Builder(
                        NetHttpTransport(),
                        GsonFactory(),
                        credential
                    ).setApplicationName("Leave Manager").build()

                    val user = _currentUser.value
                    val backupJson = """
                        {
                          "backup_date": "${System.currentTimeMillis()}",
                          "user": {
                            "name": "${user?.name}",
                            "kgid": "${user?.kgid}",
                            "email": "${user?.email}",
                            "department": "${user?.department}",
                            "district": "${user?.district}"
                          }
                        }
                    """.trimIndent()

                    val fileMetadata = File().apply {
                        name = "LeaveManager_backup_${System.currentTimeMillis()}.json"
                        mimeType = "application/json"
                        parents = listOf("appDataFolder")
                    }

                    val content = ByteArrayContent(
                        "application/json",
                        backupJson.toByteArray(Charsets.UTF_8)
                    )

                    drive.files().create(fileMetadata, content).setFields("id, name").execute()
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Backup saved to Google Drive!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
