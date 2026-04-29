package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.Person
import com.example.lendloop.data.repository.BorrowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddRecordUiState(
    val itemName: String = "",
    val personName: String = "",
    val personPhone: String = "",
    val contactUri: String = "",
    val category: String = "Other",
    val direction: Direction = Direction.LENT,
    val amount: String = "",
    val note: String = "",
    val hasDueDate: Boolean = false,
    val dueDate: Long? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddRecordViewModel @Inject constructor(
    private val repository: BorrowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecordUiState())
    val uiState: StateFlow<AddRecordUiState> = _uiState.asStateFlow()

    fun onItemNameChange(value: String) {
        _uiState.value = _uiState.value.copy(itemName = value)
    }

    fun onPersonNameChange(value: String) {
        _uiState.value = _uiState.value.copy(personName = value)
    }

    fun onPersonSelected(name: String, phone: String, uri: String) {
        _uiState.value = _uiState.value.copy(
            personName = name,
            personPhone = phone,
            contactUri = uri
        )
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun onDirectionChange(value: Direction) {
        _uiState.value = _uiState.value.copy(direction = value)
    }

    fun onAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(amount = value)
    }

    fun onNoteChange(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun onDueDateToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasDueDate = enabled,
            dueDate = if (enabled) System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) else null
        )
    }

    fun onDueDateChange(timestamp: Long) {
        _uiState.value = _uiState.value.copy(dueDate = timestamp)
    }

    fun saveRecord() {
        val state = _uiState.value
        if (state.itemName.isBlank()) {
            _uiState.value = state.copy(error = "Please enter an item name")
            return
        }
        if (state.personName.isBlank()) {
            _uiState.value = state.copy(error = "Please enter a person's name")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val personId = repository.insertPerson(
                Person(
                    name = state.personName,
                    phone = state.personPhone,
                    contactUri = state.contactUri
                )
            ).toInt()

            repository.insertRecord(
                BorrowRecord(
                    personId = personId,
                    personName = state.personName,
                    itemName = state.itemName,
                    category = state.category,
                    direction = state.direction,
                    amount = state.amount.toDoubleOrNull(),
                    dueDate = state.dueDate,
                    note = state.note.ifBlank { null }
                )
            )

            _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
        }
    }
    suspend fun canUserBorrow(): Boolean = repository.canUserBorrow()
}