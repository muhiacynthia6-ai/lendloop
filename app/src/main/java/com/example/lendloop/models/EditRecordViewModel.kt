package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.db.ElectronicsDetail
import com.example.lendloop.data.db.ItemCondition
import com.example.lendloop.data.repository.BorrowRepository
import com.example.lendloop.data.repository.ElectronicsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditRecordUiState(
    val recordId: Int = 0,
    val itemName: String = "",
    val personId: Int = 0,
    val personName: String = "",
    val personPhone: String = "",
    val contactUri: String = "",
    val category: String = "Other",
    val direction: Direction = Direction.LENT,
    val amount: String = "",
    val note: String = "",
    val hasDueDate: Boolean = false,
    val dueDate: Long? = null,
    val photoUri: String? = null,
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val condition: ItemCondition = ItemCondition.GOOD,
    val estimatedValue: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditRecordViewModel @Inject constructor(
    private val repository: BorrowRepository,
    private val electronicsRepository: ElectronicsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Int = checkNotNull(savedStateHandle["recordId"])

    private val _uiState = MutableStateFlow(EditRecordUiState(recordId = recordId))
    val uiState: StateFlow<EditRecordUiState> = _uiState.asStateFlow()

    init {
        loadRecord()
    }

    private fun loadRecord() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val record = repository.getRecordById(recordId)
            if (record != null) {
                var brand = ""
                var model = ""
                var serialNumber = ""
                var condition = ItemCondition.GOOD
                var estimatedValue = ""

                if (record.category == "Electronics") {
                    val detail = electronicsRepository.getDetailForRecord(recordId)
                    if (detail != null) {
                        brand = detail.brand
                        model = detail.model
                        serialNumber = detail.serialNumber
                        condition = detail.condition
                        estimatedValue = detail.estimatedValue?.toString() ?: ""
                    }
                }

                _uiState.value = _uiState.value.copy(
                    itemName = record.itemName,
                    personId = record.personId,
                    personName = record.personName,
                    category = record.category,
                    direction = record.direction,
                    amount = record.amount?.toString() ?: "",
                    note = record.note ?: "",
                    hasDueDate = record.dueDate != null,
                    dueDate = record.dueDate,
                    photoUri = record.photoUri,
                    brand = brand,
                    model = model,
                    serialNumber = serialNumber,
                    condition = condition,
                    estimatedValue = estimatedValue,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Record not found")
            }
        }
    }

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

    fun onPhotoUriChange(uri: String?) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun onBrandChange(value: String) {
        _uiState.value = _uiState.value.copy(brand = value)
    }

    fun onModelChange(value: String) {
        _uiState.value = _uiState.value.copy(model = value)
    }

    fun onSerialNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(serialNumber = value)
    }

    fun onConditionChange(value: ItemCondition) {
        _uiState.value = _uiState.value.copy(condition = value)
    }

    fun onEstimatedValueChange(value: String) {
        _uiState.value = _uiState.value.copy(estimatedValue = value)
    }

    fun onDueDateToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasDueDate = enabled,
            dueDate = if (enabled) _uiState.value.dueDate ?: System.currentTimeMillis() else null
        )
    }

    fun onDueDateChange(timestamp: Long) {
        _uiState.value = _uiState.value.copy(dueDate = timestamp)
    }

    fun saveChanges() {
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

            // Update record
            repository.updateRecord(
                BorrowRecord(
                    id = recordId,
                    personId = state.personId,
                    personName = state.personName,
                    itemName = state.itemName,
                    category = state.category,
                    direction = state.direction,
                    amount = state.amount.toDoubleOrNull(),
                    dueDate = state.dueDate,
                    photoUri = state.photoUri,
                    note = state.note.ifBlank { null }
                )
            )

            // Update or Insert electronics details
            if (state.category == "Electronics") {
                val existingDetail = electronicsRepository.getDetailForRecord(recordId)
                val detail = ElectronicsDetail(
                    id = existingDetail?.id ?: 0,
                    recordId = recordId,
                    brand = state.brand,
                    model = state.model,
                    serialNumber = state.serialNumber,
                    condition = state.condition,
                    estimatedValue = state.estimatedValue.toDoubleOrNull()
                )
                electronicsRepository.insertDetail(detail) // DAO uses REPLACE
            }

            _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
        }
    }
}
