package com.example.sookplace.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sookplace.data.local.dao.PlaceDao
import com.example.sookplace.data.local.entity.PlaceEntity
import com.example.sookplace.data.remote.response.SortRestaurantItem
import com.example.sookplace.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val localDao: PlaceDao
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchUiState> (SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState //SearchUiState를 계속 관찰 확인

    // 현재 검색 조건을 저장할 변수
    private var currentKeyword = ""
    private var currentCategory = "all"
    private var currentSort = "popularity"
    private var currentPage = 0

    /**
     * 검색창(Search)
     * 사용자가 검색창에 글자를 입력한 후 Enter 했을 때 싱행
     */
    fun searchByKeyword(keyword: String) {
        currentKeyword = keyword
        currentPage = 0 // 검색어가 바뀌면 첫 페이지부터

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val response = searchRepository.searchByKeyword(
                    query = currentKeyword,
                    page = currentPage
                )
                val mappedList = response.content.map { content ->
                    SortRestaurantItem(
                        id = content.id,
                        name = content.name,
                        category = "검색", // 검색 결과에는 카테고리가 없으므로 임시값
                        thumbnailUrl = content.thumbnailUrl,
                        rating = content.rating,
                        likeCount = content.likeCount,
                        isLiked = false, // 기본값
                        distanceMinutesFromCampus = null, // 기본값
                        locationName = content.address,
                        shareUrl = "" // 기본값
                    )
                }
                _searchState.value = SearchUiState.Success(mappedList)
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "검색 결과가 없습니다.")
            }
        }
    }

    /**
     * 정렬(Sort)
     * 사용자가 정렬 조건을 변경 했을 때 실행
     */
    fun fetchSortedList(category: String = currentCategory, sort: String = currentSort) {
        currentCategory = category
        currentSort = sort
        currentPage = 0

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val response = searchRepository.getFilteredList(
                    category = currentCategory,
                    sort = currentSort,
                    page = currentPage
                )

                _searchState.value = SearchUiState.Success(response.restaurants)
            } catch (e: Exception) {
                _searchState.value = SearchUiState.Error(e.message ?: "리스트를 불러오지 못했습니다.")
            }
        }
    }

    /**
     * 좋아요 로컬에 저장*/
    fun toggleLike(item: SortRestaurantItem) {
        viewModelScope.launch {
            val nextState = !item.isLiked
            updateListUI(item.id, nextState)
            //이 뒤는 백엔드
            try {
                // 2. 백엔드 서버에 좋아요 상태 전송
                // searchRepository.postLike(item.id, nextState)

                // 참고: 서버 API 호출이 실패할 경우를 대비해
                // catch 블록에서 다시 updateListUI(item.id, !nextState)로 롤백 로직을 넣을 수 있습니다.
            } catch (e: Exception) {
                // 서버 통신 실패 시 UI 복구 (선택 사항)
                updateListUI(item.id, !nextState)
            }
        }
    }

    private fun updateListUI(restaurantId: Int, nextState: Boolean) {
        val currentState = _searchState.value
        if (currentState is SearchUiState.Success) {
            val newList = currentState.list.map { listItem ->
                if (listItem.id == restaurantId) {
                    listItem.copy(
                        isLiked = nextState,
                        likeCount = if (nextState) listItem.likeCount + 1 else listItem.likeCount - 1
                    )
                } else {
                    listItem
                }
            }
            //UI 갱신
            _searchState.value = SearchUiState.Success(newList)
        }
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val list: List<SortRestaurantItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}