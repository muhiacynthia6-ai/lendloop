package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.Payment
import com.example.lendloop.data.db.PaymentMethod
import com.example.lendloop.data.db.PaymentStatus
import com.example.lendloop.data.repository.MpesaRepository
import com.example.lendloop.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentStep { SELECT_METHOD, MPESA_FORM, PROCESSING, SUCCESS, FAILED }

data class PaymentUiState(
    val step: PaymentStep = PaymentStep.SELECT_METHOD,
    val selectedMethod: PaymentMethod? = null,
    val phoneNumber: String = "",
    val paypalEmail: String = "",
    val amount: Double = 0.0,
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

    private val recordId: Int = checkNotNull(savedStateHandle["recordId"])
    private val amount: Double = checkNotNull(savedStateHandle["amount"]).toString().toDouble()
    private val personName: String = savedStateHandle["personName"] ?: ""

    private val _uiState = MutableStateFlow(PaymentUiState(amount = amount))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onMethodSelected(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(
            selectedMethod = method,
            step = when (method) {
                PaymentMethod.MPESA -> PaymentStep.MPESA_FORM
                else -> PaymentStep.SELECT_METHOD
            }
        )
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun initiateMpesaPayment() {
        val state = _uiState.value
        if (state.phoneNumber.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter your phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, step = PaymentStep.PROCESSING)

            val result = mpesaRepository.initiateStkPush(
                phoneNumber = state.phoneNumber,
                amount = state.amount,
                accountRef = "LendLoop-$recordId"
            )

            result.fold(
                onSuccess = { response ->
                    paymentRepository.insertPayment(
                        Payment(
                            recordId = recordId,
                            method = PaymentMethod.MPESA,
                            amount = state.amount,
                            status = PaymentStatus.PENDING,
                            mpesaRef = response.CheckoutRequestID
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = PaymentStep.SUCCESS,
                        mpesaRef = response.CheckoutRequestID ?: ""
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = PaymentStep.FAILED,
                        errorMessage = "M-Pesa request failed: ${it.message}"
                    )
                }
            )
        }
    }

    fun completePayPalRedirect() {
        _uiState.value = _uiState.value.copy(step = PaymentStep.SUCCESS)
    }

    fun confirmCashPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            paymentRepository.insertPayment(
                Payment(
                    recordId = recordId,
                    method = PaymentMethod.CASH,
                    amount = amount,
                    status = PaymentStatus.CONFIRMED,
                    paidAt = System.currentTimeMillis()
                )
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = PaymentStep.SUCCESS
            )
        }
    }

    fun retryPayment() {
        _uiState.value = _uiState.value.copy(
            step = PaymentStep.SELECT_METHOD,
            errorMessage = null
        )
    }

    fun getPersonName(): String = personName
    fun getAmount(): Double = amount
}
