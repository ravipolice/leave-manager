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

    // One-shot event: emits once after a successful leave save
    private val _saveSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()

    fun refreshData(user: User) {
        viewModelScope.launch {
            _uiState.value = LeaveUiState.Loading
            try {
                var currentBalance = leaveRepository.getLeaveBalance(user.kgid)
                currentBalance = checkAndApplyAutoCredits(user.kgid, currentBalance)
                
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
                    _saveSuccess.emit(Unit) // One-shot event — fires exactly once
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

    fun updateClLimit(user: User, newLimit: Int) {
        viewModelScope.launch {
            try {
                val currentBalance = _balance.value ?: leaveRepository.getLeaveBalance(user.kgid)
                _uiState.value = LeaveUiState.Loading
                
                // Keep the CL Year logic intact, just update the limit and remaining balance proportionally
                val diff = newLimit - currentBalance.clAnnualLimit
                val updatedBalance = currentBalance.copy(
                    clAnnualLimit = newLimit,
                    clRemaining = currentBalance.clRemaining + diff
                )
                
                leaveRepository.saveLeaveBalance(updatedBalance)
                _balance.value = updatedBalance
                refreshData(user)
            } catch (e: Exception) {
                 _uiState.value = LeaveUiState.Error("Failed to update CL limit: ${e.message}")
            }
        }
    }

    fun updateInitialBalances(user: User, newEl: Double, newHpl: Double) {
        viewModelScope.launch {
            try {
                val currentBalance = _balance.value ?: leaveRepository.getLeaveBalance(user.kgid)
                _uiState.value = LeaveUiState.Loading

                val updatedBalance = currentBalance.copy(
                    elManualBalance = newEl,
                    elBalance = newEl,
                    hplBalance = newHpl
                )
                leaveRepository.saveLeaveBalance(updatedBalance)
                _balance.value = updatedBalance
                refreshData(user)
            } catch (e: Exception) {
                _uiState.value = LeaveUiState.Error("Failed to update balances: ${e.message}")
            }
        }
    }

    private suspend fun checkAndApplyAutoCredits(kgid: String, balance: LeaveBalance): LeaveBalance {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) // 0-indexed, Jan = 0, Jul = 6
        
        // Define the target credit string format: "YYYY-1" for Jan, "YYYY-2" for July
        val currentSem = if (currentMonth < java.util.Calendar.JULY) 1 else 2
        
        var tempBalance = balance
        var modified = false

        // Example: If it's August 2026, currentSem = 2.
        // We need to check if we've credited for Jan 2026 and Jul 2026.
        // Instead of a complex loop, let's just do a simple check:
        // What is the expected last credit string? -> targetCredit = "${currentYear}-${currentSem}"
        val targetCredit = "${currentYear}-${currentSem}"
        
        // If balance indicates we haven't reached this target credit yet, we apply it.
        // To be thorough, if they somehow missed Jan and open the app in Jul, we should give both.
        // For simplicity, if lastElHplCreditDate is empty, we don't retroactively credit 10 years.
        // We just pretend we're up to date OR we credit once for the current semester.
        if (balance.lastElHplCreditDate.isEmpty()) {
            return balance.copy(lastElHplCreditDate = targetCredit).also {
                leaveRepository.saveLeaveBalance(it)
            }
        }

        // Parse last credit Date
        val parts = balance.lastElHplCreditDate.split("-")
        if (parts.size == 2) {
            val lastYear = parts[0].toIntOrNull() ?: currentYear
            val lastSem = parts[1].toIntOrNull() ?: currentSem
            
            // Difference in semesters
            val semsPassed = ((currentYear - lastYear) * 2) + (currentSem - lastSem)
            
            if (semsPassed > 0) {
                // We add 15 EL and 10 HPL for each semester passed (cap it to some reasonable number if needed, e.g., max 6 sems)
                val semsToCredit = semsPassed.coerceAtMost(6) // cap at 3 years missed
                tempBalance = tempBalance.copy(
                    elBalance = tempBalance.elBalance + (15.0 * semsToCredit),
                    hplBalance = tempBalance.hplBalance + (10.0 * semsToCredit),
                    lastElHplCreditDate = targetCredit
                )
                modified = true
            }
        }

        if (modified) {
            leaveRepository.saveLeaveBalance(tempBalance)
        }
        
        return tempBalance
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
