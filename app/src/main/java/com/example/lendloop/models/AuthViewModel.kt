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

/**
 * ViewModel for Authentication (Login and Registration)
 */
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

    fun register(name: String, phone: String, pin: String, confirmPin: String) {
        if (name.isBlank()) { setError("Please enter your name"); return }
        if (phone.isBlank()) { setError("Please enter your phone number"); return }
        if (pin.length < 4) { setError("PIN must be at least 4 digits"); return }
        if (pin != confirmPin) { setError("PINs do not match"); return }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(name, phone, pin)
            result.fold(
                onSuccess = { user ->
                    sessionManager.saveSession(user.id, user.name)
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { setError(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(phone: String, pin: String) {
        if (phone.isBlank()) { setError("Please enter your phone number"); return }
        if (pin.isBlank()) { setError("Please enter your PIN"); return }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(phone, pin)
            result.fold(
                onSuccess = { user ->
                    sessionManager.saveSession(user.id, user.name)
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { setError(it.message ?: "Login failed") }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun setError(message: String) {
        _uiState.value = AuthUiState(error = message)
    }
}
