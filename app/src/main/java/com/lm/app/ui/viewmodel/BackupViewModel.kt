package com.lm.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lm.app.backup.BackupService
import com.lm.app.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupService: BackupService
) : ViewModel() {

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus = _backupStatus.asStateFlow()

    fun performBackup(context: Context, user: User) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.InProgress
            val result = backupService.performBackup(context, user)
            _backupStatus.value = if (result) BackupStatus.Success else BackupStatus.Error("Backup failed. Check network or permissions.")
        }
    }

    fun performRestore(user: User, onComplete: () -> Unit) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.InProgress
            val result = backupService.restoreBackup(user)
            if (result) {
                _backupStatus.value = BackupStatus.Success
                onComplete()
            } else {
                _backupStatus.value = BackupStatus.Error("Restore failed. No backup found or connection error.")
            }
        }
    }

    fun resetStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}

sealed class BackupStatus {
    object Idle : BackupStatus()
    object InProgress : BackupStatus()
    object Success : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}
