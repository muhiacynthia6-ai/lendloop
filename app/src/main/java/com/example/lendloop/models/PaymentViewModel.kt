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

enum class PaymentStep {
    CONFIRM, PROCESSING, SUCCESS, FAILED
}

data class PaymentUiState(
    val step: PaymentStep = PaymentStep.CONFIRM,
    val amount: Double = 0.0,
    val personName: String = "",
    val phoneNumber: String = "",
    val mpesaRef: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val mpesaRepository: MpesaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Int = savedStateHandle["recordId"] ?: 0

    private val amount: Double =
        savedStateHandle.get<String>("amount")?.toDoubleOrNull() ?: 0.0

    private val personName: String =
        savedStateHandle.get<String>("personName") ?: ""

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            amount = amount,
            personName = personName
        )
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun confirmCashPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, step = PaymentStep.PROCESSING) }

            paymentRepository.insertPayment(
                Payment(
                    recordId = recordId,
                    method = PaymentMethod.CASH,
                    amount = amount,
                    status = PaymentStatus.CONFIRMED,
                    paidAt = System.currentTimeMillis()
                )
            )

            _uiState.update {
                it.copy(isLoading = false, step = PaymentStep.SUCCESS)
            }
        }
    }

    fun retryPayment() {
        _uiState.update {
            it.copy(step = PaymentStep.CONFIRM, errorMessage = null)
        }
    }
}