package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
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

data class PersonUiState(
    val person: Person? = null,
    val records: List<BorrowRecord> = emptyList(),
    val totalLent: Double = 0.0,
    val totalBorrowed: Double = 0.0,
    val netBalance: Double = 0.0
)

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val repository: BorrowRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Int = checkNotNull(savedStateHandle["personId"])

    private val _uiState = MutableStateFlow(PersonUiState())
    val uiState: StateFlow<PersonUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val person = repository.getPersonById(personId)
            repository.getRecordsByPerson(personId).collect { records ->
                val activeRecords = records.filter {
                    it.status.name == "ACTIVE"
                }
                val totalLent = activeRecords
                    .filter { it.direction == Direction.LENT }
                    .sumOf { it.amount ?: 0.0 }
                val totalBorrowed = activeRecords
                    .filter { it.direction == Direction.BORROWED }
                    .sumOf { it.amount ?: 0.0 }
                _uiState.value = PersonUiState(
                    person = person,
                    records = records,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    netBalance = totalLent - totalBorrowed
                )
            }
        }
    }

    fun markReturned(id: Int) {
        viewModelScope.launch {
            repository.markReturned(id)
        }
    }

    fun updateLastReminded(id: Int) {
        viewModelScope.launch {
            repository.updateLastReminded(id)
        }
    }
}