package com.lm.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lm.app.data.User
import com.lm.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _navigateToRegister = MutableStateFlow(false)
    val navigateToRegister = _navigateToRegister.asStateFlow()

    private val _pendingGoogleUser = MutableStateFlow<String?>(null)
    val pendingGoogleUser = _pendingGoogleUser.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent = _otpSent.asStateFlow()

    private val _otpCountdown = MutableStateFlow(0)
    val otpCountdown = _otpCountdown.asStateFlow()

    fun login(identifier: String, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = authRepository.loginWithEmailPin(identifier, pin)
            result.onSuccess { user ->
                _currentUser.value = user
            }.onFailure { exception ->
                _error.value = exception.message ?: "Login failed"
            }
            
            _isLoading.value = false
        }
    }

    fun register(user: User, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.registerUser(user, pin)
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Registration failed"
            }

            _isLoading.value = false
        }
    }

    fun sendOtp(email: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.sendOtp(email)
            result.onSuccess { message ->
                _otpSent.value = true
                onSuccess(message)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to send OTP"
            }

            _isLoading.value = false
        }
    }

    fun sendPasswordResetOtp(email: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.sendOtp(email)
            result.onSuccess { message ->
                onSuccess(true)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to send OTP"
                onSuccess(false)
            }

            _isLoading.value = false
        }
    }

    fun verifyOtp(otp: String) {
        // Mock method for handling OTP verification during login flow
        _error.value = "OTP Login flow is currently not fully implemented with Firebase"
    }

    fun verifyOtpAndResetPin(email: String, otp: String, newPin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.verifyOtpAndResetPin(email, otp, newPin)
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to reset PIN"
            }

            _isLoading.value = false
        }
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }

    fun clearOtpState() {
        _otpSent.value = false
        _otpCountdown.value = 0
    }

    fun clearNavigateToRegister() {
        _navigateToRegister.value = false
    }

    fun clearPendingGoogleUser() {
        _pendingGoogleUser.value = null
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess { user ->
                _currentUser.value = user
            }.onFailure { exception ->
                if (exception.message?.startsWith("USER_NOT_FOUND") == true) {
                    val parts = exception.message!!.split(":")
                    if (parts.size >= 3) {
                        _pendingGoogleUser.value = parts[2] // Only save email string
                    }
                    _navigateToRegister.value = true
                } else {
                    _error.value = exception.message ?: "Google Sign-In failed"
                }
            }

            _isLoading.value = false
        }
    }
}
