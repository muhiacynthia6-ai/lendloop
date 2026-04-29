package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.repository.BorrowRepository
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val lentOut: List<BorrowRecord> = emptyList(),
    val borrowed: List<BorrowRecord> = emptyList(),
    val totalLentAmount: Double = 0.0,
    val totalBorrowedAmount: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BorrowRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState = repository.getActiveRecords()
        .map { records ->
            val lentOut = records.filter { it.direction == Direction.LENT }
            val borrowed = records.filter { it.direction == Direction.BORROWED }
            HomeUiState(
                lentOut = lentOut,
                borrowed = borrowed,
                totalLentAmount = lentOut.sumOf { it.amount ?: 0.0 },
                totalBorrowedAmount = borrowed.sumOf { it.amount ?: 0.0 }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun markReturned(id: Int) {
        viewModelScope.launch {
            repository.markReturned(id)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
