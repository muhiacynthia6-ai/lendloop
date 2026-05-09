package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.repository.AuthRepository
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank()) {
            setError("Please enter your name"); return
        }
        if (email.isBlank()) {
            setError("Please enter your email"); return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError("Please enter a valid email"); return
        }
        if (password.length < 6) {
            setError("Password must be at least 6 characters"); return
        }
        if (password != confirmPassword) {
            setError("Passwords do not match"); return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.register(name, email, password).fold(
                onSuccess = { user ->
                    sessionManager.saveSession(user.id, user.name)
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { setError(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            setError("Please enter your email"); return
        }
        if (password.isBlank()) {
            setError("Please enter your password"); return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.login(email, password).fold(
                onSuccess = { user ->
                    sessionManager.saveSession(user.id, user.name)
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { setError(it.message ?: "Login failed") }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            setError("Please enter your email"); return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError("Please enter a valid email"); return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.sendPasswordReset(email).fold(
                onSuccess = { _uiState.value = AuthUiState(isSuccess = true) },
                onFailure = { setError(it.message ?: "Failed to send reset email") }
            )
        }
    }

    fun clearError()  { _uiState.value = _uiState.value.copy(error = null) }
    fun resetState()  { _uiState.value = AuthUiState() }

    private fun setError(message: String) {
        _uiState.value = AuthUiState(error = message)
    }
}
