package com.example.lendloop.models

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.Review
import com.example.lendloop.data.repository.ReviewRepository
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val alreadyReviewed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Int  = checkNotNull(savedStateHandle["recordId"])
    private val revieweeId: Int = checkNotNull(savedStateHandle["revieweeId"])

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        checkIfAlreadyReviewed()
    }

    private fun checkIfAlreadyReviewed() {
        viewModelScope.launch {
            val hasReview = reviewRepository.hasReview(recordId)
            if (hasReview) {
                _uiState.value = _uiState.value.copy(alreadyReviewed = true)
            }
        }
    }

    fun onRatingChange(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating, error = null)
    }

    fun onCommentChange(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun submitReview() {
        val state = _uiState.value
        if (state.rating == 0) {
            _uiState.value = state.copy(error = "Please select a star rating")
            return
        }

        val reviewerId = sessionManager.getUserId()
        if (reviewerId == null) {
            _uiState.value = state.copy(error = "Not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            reviewRepository.submitReview(
                Review(
                    recordId   = recordId,
                    reviewerId = reviewerId,
                    revieweeId = revieweeId.toString(),
                    rating     = state.rating,
                    comment    = state.comment.ifBlank { null }
                )
            )
            _uiState.value = _uiState.value.copy(isLoading = false, isSubmitted = true)
        }
    }

    fun skipReview() {
        _uiState.value = _uiState.value.copy(isSubmitted = true)
    }
}
