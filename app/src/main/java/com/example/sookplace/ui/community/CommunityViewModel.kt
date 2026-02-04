package com.example.sookplace.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sookplace.data.local.entity.PostEntity
import com.example.sookplace.data.remote.response.PostContent
import com.example.sookplace.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {


    /***피드 조회*/
    //상태 저장
    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState

    //페이지 정보
    private var currentPage = 0
    private var isLastPage = false
    private var isFetching = false

    //기존 데이터 리스트 보관
    private val allPosts = mutableListOf<PostContent>()

    init {
        fetchPosts()
    }

    fun fetchPosts(isRefresh: Boolean = false) {
        if (isFetching || (isLastPage && !isRefresh)) return

        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading

            isFetching = true
            if (isRefresh) { //새로고침
                currentPage = 0
                isLastPage = false
            }

            try {
                //최신순(createdAt,desc)으로 10개 가져오기
                val response = communityRepository.getCommunityFeed(
                    category = null,
                    sort = "createdAt,desc",
                    page = currentPage,
                    size = 10
                )

                if (isRefresh) { //새로고침 -> 초기화
                    allPosts.clear()
                }

                allPosts.addAll(response.content) //추가된 리스트를 append

                _uiState.value = CommunityUiState.Success(allPosts.toList())

                currentPage++
                isLastPage = !response.hasNext
            } catch (e: Exception) {
                _uiState.value = CommunityUiState.Error(e.message ?: "게시글을 불러오는 데 실패했습니다.")
            } finally {
                isFetching = false
            }
        }
    }

    // 좋아요 토글 로직
    fun toggleLike(post: PostContent) {
        viewModelScope.launch {
            val newLikedByMe = !post.likedByMe
            val newLikeCount = if (newLikedByMe) post.likeCount + 1 else post.likeCount - 1

            val index = allPosts.indexOfFirst { it.postId == post.postId }
            if (index != -1) {
                allPosts[index] = allPosts[index].copy(
                    likedByMe = newLikedByMe,
                    likeCount = newLikeCount
                )
                _uiState.value = CommunityUiState.Success(allPosts.toList())
            }

            try {
//                val isSuccess = communityRepository.postLike(post.postId)
//
//                if (newLikedByMe) communityRepository.insertLocalLike(post)
//                else communityRepository.deleteLocalLike(post.postId)

            } catch (e: Exception) {
                fetchPosts(isRefresh = true) // 실패 시 리스트 새로고침 등으로 대응
            }
        }
    }

    // 북마크 토글 로직
    fun toggleBookmark(post: PostContent) {
        viewModelScope.launch {
            val newBookmarked = !(post.isBookmarked ?: false)

            val index = allPosts.indexOfFirst { it.postId == post.postId }
            if (index != -1) {
                allPosts[index] = allPosts[index].copy(isBookmarked = newBookmarked)
                _uiState.value = CommunityUiState.Success(allPosts.toList())
            }

            if (newBookmarked) { //북마크된 게시물 저장(마이포스트용)
                val entity = PostEntity(
                    postId = post.postId,
                    userId = post.author.userId,
                    nickname = post.author.nickname,
                    profileImageUrl = post.author.profileImageUrl,
                    title = post.title,
                    excerpt = post.excerpt,
                    category = post.category,
                    placeId = post.place.placeId,
                    placeName = post.place.name,
                    rating = post.rating,
                    imageUrl = post.imageUrl,
                    likeCount = post.likeCount,
                    commentCount = post.commentCount,
                    displayTime = post.displayTime
                )
                communityRepository.saveBookmark(entity)
            } else {
                communityRepository.removeBookmark(post.postId)
            }
        }
    }


    //UI 상태 정의 (Success 내부에 담긴 데이터는 PostContent 리스트)
    sealed class CommunityUiState {
        object Loading : CommunityUiState()
        data class Success(val posts: List<PostContent>) : CommunityUiState()
        data class Error(val message: String) : CommunityUiState()
    }

}