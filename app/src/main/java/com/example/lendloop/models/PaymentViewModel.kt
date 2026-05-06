package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.*
import com.example.lendloop.data.repository.MpesaRepository
import com.example.lendloop.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentStep { SELECT_METHOD, CONFIRM, PROCESSING, SUCCESS, FAILED }

data class PaymentUiState(
    val step: PaymentStep         = PaymentStep.SELECT_METHOD,
    val amount: Double            = 0.0,
    val personName: String        = "",
    val selectedMethod: PaymentMethod? = null,
    val phoneNumber: String       = "",
    val mpesaMessage: String?     = null,
    val paypalEmail: String       = "",
    val errorMessage: String?     = null,
    val isLoading: Boolean        = false
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val mpesaRepository: MpesaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Int     = savedStateHandle["recordId"] ?: 0
    private val amount: Double    = savedStateHandle.get<String>("amount")?.toDoubleOrNull() ?: 0.0
    private val personName: String = savedStateHandle.get<String>("personName") ?: ""

    private val _uiState = MutableStateFlow(
        PaymentUiState(amount = amount, personName = personName)
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method, step = PaymentStep.CONFIRM, errorMessage = null) }
    }

    fun updatePhone(phone: String)        { _uiState.update { it.copy(phoneNumber = phone) } }
    fun updatePaypalEmail(email: String)  { _uiState.update { it.copy(paypalEmail = email) } }
    fun confirmPayment() {
        when (_uiState.value.selectedMethod) {
            PaymentMethod.CASH   -> confirmCashPayment()
            PaymentMethod.MPESA  -> initiateMpesaPayment()
            PaymentMethod.PAYPAL -> confirmPaypalPayment()
            null                 -> _uiState.update { it.copy(errorMessage = "Please select a payment method") }
        }
    }
    private fun confirmCashPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(step = PaymentStep.PROCESSING, isLoading = true) }
            paymentRepository.insertPayment(
                Payment(
                    recordId = recordId,
                    method   = PaymentMethod.CASH,
                    amount   = amount,
                    status   = PaymentStatus.CONFIRMED,
                    paidAt   = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(step = PaymentStep.SUCCESS, isLoading = false) }
        }
    }
    private fun initiateMpesaPayment() {
        val phone = _uiState.value.phoneNumber.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your M-Pesa phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(step = PaymentStep.PROCESSING, isLoading = true, errorMessage = null) }

            val result = mpesaRepository.initiateStkPush(
                phoneNumber = phone,
                amount      = amount,
                accountRef  = "LendLoop-$recordId"
            )

            result.fold(
                onSuccess = { response ->
                    paymentRepository.insertPayment(
                        Payment(
                            recordId  = recordId,
                            method    = PaymentMethod.MPESA,
                            amount    = amount,
                            status    = PaymentStatus.PENDING,
                            mpesaRef  = response.CheckoutRequestID
                        )
                    )
                    _uiState.update {
                        it.copy(
                            step         = PaymentStep.SUCCESS,
                            isLoading    = false,
                            mpesaMessage = response.CustomerMessage ?: "Check your phone to complete payment"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            step         = PaymentStep.FAILED,
                            isLoading    = false,
                            errorMessage = error.message ?: "M-Pesa payment failed"
                        )
                    }
                }
            )
        }
    }
    private fun confirmPaypalPayment() {
        val email = _uiState.value.paypalEmail.trim()
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Enter a valid PayPal email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(step = PaymentStep.PROCESSING, isLoading = true) }
            paymentRepository.insertPayment(
                Payment(
                    recordId     = recordId,
                    method       = PaymentMethod.PAYPAL,
                    amount       = amount,
                    status       = PaymentStatus.CONFIRMED,
                    paypalEmail  = email,
                    paidAt       = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(step = PaymentStep.SUCCESS, isLoading = false) }
        }
    }
    fun retryPayment() {
        _uiState.update { it.copy(step = PaymentStep.CONFIRM, errorMessage = null) }
    }

    fun backToMethodSelect() {
        _uiState.update {
            it.copy(step = PaymentStep.SELECT_METHOD, selectedMethod = null, errorMessage = null)
        }
    }
}