package com.example.lendloop.models   // ✅ Fixed: was com.example.lendloop.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.repository.MpesaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MpesaState {
    object Idle    : MpesaState()
    object Loading : MpesaState()
    data class Success(val message: String) : MpesaState()
    data class Error(val message: String)   : MpesaState()
}

@HiltViewModel
class MpesaViewModel @Inject constructor(
    private val repository: MpesaRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MpesaState>(MpesaState.Idle)
    val state: StateFlow<MpesaState> = _state.asStateFlow()

    fun pay(phoneNumber: String, amount: Double, accountRef: String = "LendLoop") {
        if (phoneNumber.isBlank()) {
            _state.value = MpesaState.Error("Please enter a phone number")
            return
        }
        if (amount <= 0) {
            _state.value = MpesaState.Error("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _state.value = MpesaState.Loading

            val result = repository.initiateStkPush(
                phoneNumber = phoneNumber,
                amount      = amount,
                accountRef  = accountRef
            )

            _state.value = result.fold(
                onSuccess = {
                    MpesaState.Success(it.CustomerMessage ?: "Check your phone to complete payment")
                },
                onFailure = {
                    MpesaState.Error(it.message ?: "M-Pesa payment failed")
                }
            )
        }
    }

    fun reset() { _state.value = MpesaState.Idle }
}