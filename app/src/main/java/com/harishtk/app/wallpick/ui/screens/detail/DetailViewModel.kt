package com.harishtk.app.wallpick.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import com.harishtk.app.wallpick.data.succeeded
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted photoId: Int,
    repository: WallpaperRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState> = repository.getPhotoById(photoId).mapLatest { result ->
        when (result) {
            is Result.Loading -> UiState(loading = true)
            is Result.Error -> UiState(error = result.exception)
            is Result.Success -> {
                if (result.succeeded) {
                    UiState(photo = result.data)
                } else {
                    UiState(error = IllegalStateException("No data"))
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState(loading = true)
    )

    @AssistedFactory
    interface Factory {
        fun create(photoId: Int): DetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            photoId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(photoId) as T
            }
        }
    }
}

data class UiState(
    val error: Exception? = null,
    val photo: Photo? = null,
    val loading: Boolean = false
)