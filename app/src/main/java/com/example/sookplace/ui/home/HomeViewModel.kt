package com.example.sookplace.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sookplace.data.local.entity.FeaturedRestaurantEntity
import com.example.sookplace.data.remote.response.Category
import com.example.sookplace.data.remote.response.NextAction
import com.example.sookplace.data.remote.response.Restaurant
import com.example.sookplace.data.remote.response.RouletteSpinResponse
import com.example.sookplace.data.repository.RestaurantRepository
import com.example.sookplace.data.repository.RouletteRepository
import com.example.sookplace.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val restaurantRepository: RestaurantRepository,
    private val rouletteRepository: RouletteRepository
) : ViewModel() {
    //프로필
    val userProfile = userProfileRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            userProfileRepository.refreshIfNeeded()
        }
    }

    //오늘의 숙플레이스
    val featuredRestaurants: StateFlow<List<FeaturedRestaurantEntity>> =
        restaurantRepository.featuredRestaurants // Repository에서 가져온 Flow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        // 앱 실행 시 숙플레이스 정보를 갱신 시도
        viewModelScope.launch {
            userProfileRepository.refreshIfNeeded()
            loadFeaturedRestaurants()
        }
    }
    //디버깅용 플래그
    private val USE_DUMMY = true

    fun loadFeaturedRestaurants() {
        viewModelScope.launch {
            if (USE_DUMMY) { //디버깅용//TODO: 백엔드 연결후 삭제
                val dummyList = listOf(
                    FeaturedRestaurantEntity("숙대입구 핀치", "서울 용산구 청파로47길 52", "https://picsum.photos/id/102/400/300", true),
                    FeaturedRestaurantEntity("청파쌍대포", "서울 용산구 청파로 291", "https://picsum.photos/id/292/400/300", false),
                    FeaturedRestaurantEntity("미소콩", "서울 용산구 청파로45길 19", "https://picsum.photos/id/429/400/300", true),
                    FeaturedRestaurantEntity("구복만두", "서울 용산구 두텁바위로 7", "https://picsum.photos/id/488/400/300", false),
                    FeaturedRestaurantEntity("효뜨", "서울 용산구 한강대로40가길 6", "https://picsum.photos/id/635/400/300", true)
                )
                restaurantRepository.updateDummyData(dummyList)
                return@launch
            }

            try {
                restaurantRepository.refreshFeaturedRestaurants()
            } catch (e: Exception) {
                // 오류 처리: Toast, Log, State 업데이트 등
            }
        }
    }

    //룰렛 돌리기
    private val _excludedRestaurantIds = mutableListOf<Int>()

    private val _rouletteState = MutableStateFlow<RouletteUiState>(RouletteUiState.Idle)
    val rouletteState: StateFlow<RouletteUiState> = _rouletteState

    fun spinRoulette(mode: String) {
        viewModelScope.launch {
            _rouletteState.value = RouletteUiState.Loading // 로딩 시작

            delay(2000) //가짜 로딩 시간//TODO: 백엔드와 연결 후 삭제할 것
            val categories = listOf("치킨", "카페", "한식", "분식", "양식", "디저트")

            val dummyResponse = when (mode) {
                "category" -> {
                    // 카테고리 결과 모드일 때
                    val randomCategoryName = categories.random()
                    RouletteSpinResponse(
                        type = "CATEGORY",
                        restaurant = null,
                        category = Category(
                            key = "category_key",
                            name = randomCategoryName
                        ),
                        nextAction = NextAction(detailUrl = "", searchUrl = "")
                    )
                }

                else -> {
                    // 식당 결과 모드일 때 (myplace, top20)
                    val randomId = (100..200).random()
                    RouletteSpinResponse(
                        type = "RESTAURANT",
                        restaurant = Restaurant(
                            id = 1,
                            name = "숙대 앞 맛집 $randomId",
                            category = "일식",
                            thumbnailUrl = "https://picsum.photos/id/$randomId/400/300", // 랜덤 이미지
                            rating = (3..5).random().toDouble(),
                            distanceMinutesFromCampus = (1..10).random()
                        ),
                        category = null,
                        nextAction = NextAction(detailUrl = "https://example.com", searchUrl = "")
                    )
                }
            }

            _rouletteState.value = RouletteUiState.Success(dummyResponse)
            dummyResponse.restaurant?.let { _excludedRestaurantIds.add(it.id) }

            try {
                val response = rouletteRepository.spin(mode, _excludedRestaurantIds)
                _rouletteState.value = RouletteUiState.Success(response)

                response.restaurant?.let { //중복 제거 식당 리스트
                    _excludedRestaurantIds.add(it.id)
                }
            } catch (e: Exception) {
                _rouletteState.value = RouletteUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    // 상태 초기화 (결과창을 닫거나 다시 시도할 때 사용)
    fun clearExcludedIds() {
        _excludedRestaurantIds.clear()
    }

    fun resetRouletteState() {
        _rouletteState.value = RouletteUiState.Idle
    }

}

sealed class RouletteUiState { //룰렛 상태 정의
    object Idle : RouletteUiState() // 아무것도 안 한 상태
    object Loading : RouletteUiState() // 서버 응답 대기 중
    data class Success(val data: RouletteSpinResponse) : RouletteUiState() // 성공
    data class Error(val message: String) : RouletteUiState() // 실패
}