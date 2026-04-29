package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.BorrowRecord
import com.example.lendloop.data.db.Direction
import com.example.lendloop.data.repository.BorrowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class HistoryFilter { ALL, LENT, BORROWED }

data class HistoryUiState(
    val records: List<BorrowRecord> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.ALL
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: BorrowRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)

    val uiState = combine(
        repository.getReturnedRecords(),
        _filter
    ) { records, filter ->
        val filtered = when (filter) {
            HistoryFilter.ALL -> records
            HistoryFilter.LENT -> records.filter { it.direction == Direction.LENT }
            HistoryFilter.BORROWED -> records.filter { it.direction == Direction.BORROWED }
        }
        HistoryUiState(records = filtered, filter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }
}