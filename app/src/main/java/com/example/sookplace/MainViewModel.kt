package com.example.sookplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sookplace.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel  @Inject constructor(
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val isLoggedIn = userProfileRepository.userProfileFlow
        .map { it != null }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )
}