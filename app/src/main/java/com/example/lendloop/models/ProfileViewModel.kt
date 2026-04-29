package com.example.lendloop.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lendloop.data.db.Review
import com.example.lendloop.data.db.TrustScore
import com.example.lendloop.data.repository.ReviewRepository
import com.example.lendloop.data.repository.TrustScoreRepository
import com.example.lendloop.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val averageRating: Float = 0f,
    val reviews: List<Review> = emptyList(),
    val trustScore: TrustScore? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val trustScoreRepository: TrustScoreRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        val userName = sessionManager.getUserName() ?: "User"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userName = userName) }

            val trustScoreFlow = trustScoreRepository.observeTrustScore(userId)
            val reviewsFlow = reviewRepository.getReviewsForUser(userId)
            val avgRating = reviewRepository.getAverageRating(userId)

            combine(trustScoreFlow, reviewsFlow) { trustScore, reviews ->
                ProfileUiState(
                    isLoading = false,
                    userName = userName,
                    averageRating = avgRating,
                    reviews = reviews,
                    trustScore = trustScore
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
