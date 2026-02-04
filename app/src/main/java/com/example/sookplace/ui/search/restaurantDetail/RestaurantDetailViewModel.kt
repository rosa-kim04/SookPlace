package com.example.sookplace.ui.search.restaurantDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sookplace.data.local.dao.PlaceDao
import com.example.sookplace.data.local.entity.PlaceEntity
import com.example.sookplace.data.remote.response.RestaurantDetailResponse
import com.example.sookplace.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val localDao: PlaceDao
) : ViewModel() {
    //상태 변수
    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailState: StateFlow<DetailUiState> = _detailState

    //종아요&&마이플레이스
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    //좋아요
    fun toggleLike(restaurantId: Int) {
        viewModelScope.launch {
            val nextState = !_isLiked.value
            _isLiked.value = nextState
            // TODO: 백엔드 API가 있다면 여기서 호출 (예: restaurantRepository.postLike(restaurantId, nextState))

        }
    }

    //마이 플레이스
    fun toggleSave(restaurantId: Int) {
        viewModelScope.launch {
            val nextState = !_isSaved.value
            _isSaved.value = nextState

            val currentState = _detailState.value
            if (currentState is DetailUiState.Success) {
                val data = currentState.data

                if (nextState) {
                    // 핀 꽂기: PlaceEntity 구조로 저장
                    val entity = PlaceEntity(
                        restaurantId = restaurantId,
                        name = data.name, // 상세 데이터의 이미지
                        category = data.category,
                        thumbnailUrl = data.thumbnailUrl,
                        rating = data.rating,
                        address = data.address,
                        addedAt = System.currentTimeMillis()
                    )
                    localDao.insertPlace(entity)
                } else {
                    // 핀 빼기: 로컬 DB에서 삭제
                    localDao.deletePlace(restaurantId)
                }
                _isSaved.value = nextState
            }
        }
    }

    fun fetchRestaurantDetail(restaurantId: Int) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            try {
                val response = restaurantRepository.getRestaurantDetail(restaurantId)
                val isSavedInLocal = localDao.isPlaceSaved(restaurantId)

//                _isLiked.value = response.isLiked //TODO: 좋아요정보가 없다
                _isSaved.value = isSavedInLocal

                _detailState.value = DetailUiState.Success(response)
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(e.message ?: "상세 정보를 불러오지 못했습니다.")
            }
        }
    }

    sealed class DetailUiState {
        object Idle : DetailUiState()
        object Loading : DetailUiState()
        data class Success(val data: RestaurantDetailResponse) : DetailUiState()
        data class Error(val message: String) : DetailUiState()
    }
}



