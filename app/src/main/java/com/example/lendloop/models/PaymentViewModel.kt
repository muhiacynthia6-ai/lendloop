package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.*
import com.example.lendloop.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentStep { SELECT_METHOD, CONFIRM, PROCESSING, SUCCESS, FAILED }

data class PaymentUiState(
    val step:           PaymentStep    = PaymentStep.SELECT_METHOD,
    val amount:         Double         = 0.0,
    val personName:     String         = "",
    val selectedMethod: PaymentMethod? = null,
    val errorMessage:   String?        = null,
    val isLoading:      Boolean        = false
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    savedStateHandle:              SavedStateHandle
) : ViewModel() {

    private val recordId:   Int    = savedStateHandle["recordId"] ?: 0
    private val amount:     Double = savedStateHandle.get<String>("amount")?.toDoubleOrNull() ?: 0.0
    private val personName: String = savedStateHandle.get<String>("personName") ?: ""

    private val _uiState = MutableStateFlow(
        PaymentUiState(amount = amount, personName = personName)
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    // Cash is the only supported method — auto-select it and go to CONFIRM
    fun selectMethod(method: PaymentMethod) {
        _uiState.update {
            it.copy(
                selectedMethod = PaymentMethod.CASH,
                step           = PaymentStep.CONFIRM,
                errorMessage   = null
            )
        }
    }

    fun confirmPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(step = PaymentStep.PROCESSING, isLoading = true) }
            try {
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        step         = PaymentStep.FAILED,
                        isLoading    = false,
                        errorMessage = e.message ?: "Something went wrong"
                    )
                }
            }
        }
    }

    fun retryPayment()      { _uiState.update { it.copy(step = PaymentStep.CONFIRM,        errorMessage = null) } }
    fun backToMethodSelect() { _uiState.update { it.copy(step = PaymentStep.SELECT_METHOD, selectedMethod = null, errorMessage = null) } }
}