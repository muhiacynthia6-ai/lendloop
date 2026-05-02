package com.example.lendloop.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.repository.MpesaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ────────────────────────────────────────────
sealed class PaymentState {
    object Idle       : PaymentState()
    object Loading    : PaymentState()
    data class Success(val message: String) : PaymentState()
    data class Error(val message: String)   : PaymentState()
}

@HiltViewModel
class MpesaViewModel @Inject constructor(
    private val repository: MpesaRepository
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun pay(
        phoneNumber: String,
        amount: Double,
        accountRef: String = "LendLoop"
    ) {
        // Basic validation before hitting the API
        if (phoneNumber.isBlank()) {
            _paymentState.value = PaymentState.Error("Please enter a phone number")
            return
        }
        if (amount <= 0) {
            _paymentState.value = PaymentState.Error("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            val result = repository.initiateStkPush(
                phoneNumber = phoneNumber,
                amount      = amount,
                accountRef  = accountRef
            )

            _paymentState.value = result.fold(
                onSuccess = {
                    PaymentState.Success(
                        it.CustomerMessage ?: "Check your phone to complete payment"
                    )
                },
                onFailure = {
                    PaymentState.Error(it.message ?: "Payment failed")
                }
            )
        }
    }

    fun resetState() {
        _paymentState.value = PaymentState.Idle
    }
}