package com.lm.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lm.app.data.*
import com.lm.app.utils.LeaveBalanceCalculator
import com.lm.app.utils.LeaveValidationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _balance = MutableStateFlow<LeaveBalance?>(null)
    val balance: StateFlow<LeaveBalance?> = _balance.asStateFlow()

    private val _entries = MutableStateFlow<List<LeaveEntry>>(emptyList())
    val entries: StateFlow<List<LeaveEntry>> = _entries.asStateFlow()

    private val _uiState = MutableStateFlow<LeaveUiState>(LeaveUiState.Idle)
    val uiState: StateFlow<LeaveUiState> = _uiState.asStateFlow()

    private val _statistics = MutableStateFlow<LeaveStatistics?>(null)
    val statistics: StateFlow<LeaveStatistics?> = _statistics.asStateFlow()

    fun refreshData(user: User) {
        viewModelScope.launch {
            _uiState.value = LeaveUiState.Loading
            try {
                val currentBalance = leaveRepository.getLeaveBalance(user.kgid)
                _balance.value = currentBalance

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val stats = leaveRepository.getLeaveStatistics(user.kgid, currentYear)
                _statistics.value = stats

                leaveRepository.getLeaveEntries(user.kgid).collect { all ->
                    _entries.value = all
                    _uiState.value = LeaveUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun applyLeave(user: User, entry: LeaveEntry) {
        viewModelScope.launch {
            try {
                val currentBalance = _balance.value ?: leaveRepository.getLeaveBalance(user.kgid)
                _uiState.value = LeaveUiState.Loading

                val woCount = leaveRepository.getWoCountForMonth(user.kgid, entry.year, entry.month)
                val validation = LeaveValidationEngine.validateLeave(user, currentBalance, entry, woCount)

                if (validation.isSuccess) {
                    val updatedBalance = LeaveBalanceCalculator.applyLeave(currentBalance, entry)
                    leaveRepository.saveLeaveEntry(updatedBalance, entry)
                    _balance.value = updatedBalance
                    refreshData(user)
                } else {
                    _uiState.value = LeaveUiState.Error(validation.exceptionOrNull()?.message ?: "Validation failed")
                }
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error("Failed to save leave: ${e.message}")
            }
        }
    }

    fun deleteLeaveEntry(user: User, entry: LeaveEntry) {
        viewModelScope.launch {
            try {
                val currentBalance = _balance.value ?: leaveRepository.getLeaveBalance(user.kgid)
                _uiState.value = LeaveUiState.Loading

                val restoredBalance = LeaveBalanceCalculator.reverseLeave(currentBalance, entry)
                leaveRepository.deleteLeaveEntry(restoredBalance, entry)
                _balance.value = restoredBalance
                refreshData(user)
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error("Failed to delete: ${e.message}")
            }
        }
    }

    fun updateLeaveEntry(user: User, oldEntry: LeaveEntry, newEntry: LeaveEntry) {
        viewModelScope.launch {
            try {
                val currentBalance = _balance.value ?: leaveRepository.getLeaveBalance(user.kgid)
                _uiState.value = LeaveUiState.Loading

                val updatedBalance = leaveRepository.updateLeaveEntry(oldEntry, newEntry, currentBalance)
                _balance.value = updatedBalance
                refreshData(user)
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error("Failed to update: ${e.message}")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = LeaveUiState.Idle
    }

    fun seedSampleData(user: User) {
        viewModelScope.launch {
            try {
                _uiState.value = LeaveUiState.Loading
                
                // 1. Reset Balance
                var currentBalance = LeaveBalance(
                    kgid = user.kgid,
                    clYear = 2026,
                    clAnnualLimit = 15,
                    clRemaining = 15.0,
                    elBalance = 30.0,
                    hplBalance = 20.0,
                    elManualBalance = 30.0
                )

                // 2. Define Sample Entries
                val cal = Calendar.getInstance()
                val currentYear = cal.get(Calendar.YEAR)
                
                val sampleEntries = mutableListOf<LeaveEntry>()
                
                // Jan: CL
                sampleEntries.add(createSampleEntry(user.kgid, currentYear, 1, 5, 6, "CL", 2.0))
                // Feb: Half Day CL
                sampleEntries.add(createSampleEntry(user.kgid, currentYear, 2, 10, 10, "CL", 0.5, isHalfDay = true))
                // Mar: EL
                sampleEntries.add(createSampleEntry(user.kgid, currentYear, 3, 1, 10, "EL", 10.0))
                // Apr: HPL (5 calendar days, counts as 2.5 days)
                sampleEntries.add(createSampleEntry(user.kgid, currentYear, 4, 15, 19, "HPL", 2.5))
                // May: 4 WOs
                for (d in listOf(7, 14, 21, 28)) {
                    sampleEntries.add(createSampleEntry(user.kgid, currentYear, 5, d, d, "WO", 1.0))
                }
                // Jun: LWA
                sampleEntries.add(createSampleEntry(user.kgid, currentYear, 6, 1, 3, "LWA", 3.0))

                if (user.gender.lowercase() == "female") {
                    // Jul: CCL
                    sampleEntries.add(createSampleEntry(user.kgid, currentYear, 7, 1, 30, "CCL", 30.0))
                    // Aug: MCL
                    sampleEntries.add(createSampleEntry(user.kgid, currentYear, 8, 3, 3, "CL", 1.0, isMcl = true))
                    // Oct: ML
                    sampleEntries.add(createSampleEntry(user.kgid, currentYear, 10, 1, 30, "ML", 1.0)) // Counted as 1 usage
                } else {
                    // Sep: PL
                    sampleEntries.add(createSampleEntry(user.kgid, currentYear, 9, 1, 15, "PL", 15.0))
                }

                // 3. Apply and Save
                for (entry in sampleEntries) {
                    currentBalance = LeaveBalanceCalculator.applyLeave(currentBalance, entry)
                    leaveRepository.saveLeaveEntry(currentBalance, entry)
                }
                
                leaveRepository.saveLeaveBalance(currentBalance)
                _balance.value = currentBalance
                refreshData(user)
                
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error("Seeding failed: ${e.message}")
            }
        }
    }

    private fun createSampleEntry(
        kgid: String, year: Int, month: Int, dayFrom: Int, dayTo: Int,
        type: String, days: Double, isHalfDay: Boolean = false, isMcl: Boolean = false
    ): LeaveEntry {
        val from = Calendar.getInstance().apply { set(year, month - 1, dayFrom, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.time
        val to = Calendar.getInstance().apply { set(year, month - 1, dayTo, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.time
        return LeaveEntry(
            kgid = kgid,
            dateFrom = from,
            dateTo = to,
            totalDays = days,
            leaveType = type,
            remark = "Sample data entry",
            year = year,
            month = month,
            isHalfDay = isHalfDay,
            isMcl = isMcl
        )
    }
}

sealed class LeaveUiState {
    object Idle : LeaveUiState()
    object Loading : LeaveUiState()
    object Success : LeaveUiState()
    data class Error(val message: String) : LeaveUiState()
}
