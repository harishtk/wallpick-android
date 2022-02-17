package com.harishtk.app.wallpick

import androidx.compose.runtime.currentRecomposeScope
import androidx.lifecycle.*
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import com.harishtk.app.wallpick.data.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    val accept: (UiAction) -> Unit

    private var _curatedPhotosResponse: MutableLiveData<Result<CuratedResponse>> = MutableLiveData()

    init {

        val actionStateFlow = MutableSharedFlow<UiAction>()

        viewModelScope.launch {
            repository.getCuratedFlow(1)
                .catch { exception -> Timber.e(exception) }
                .collect { values ->
                    when {
                        values.succeeded -> {
                            _uiState.emit(UiState(photosList = (values as Result.Success).data.photos))
                        }
                        values is Result.Error -> {
                            _uiState.emit(UiState(error = values.exception))
                        }
                        else -> {
                            _uiState.emit(UiState(loading = true))
                        }
                    }
                }
        }

        accept = { action ->
            Timber.d("$action")
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }
    val curatedPhotosResponse: LiveData<Result<CuratedResponse>> = _curatedPhotosResponse

    fun fetchCuratedPhotos() = viewModelScope.launch {
        repository.getCuratedFlow(1)
            .catch { exception -> Timber.e(exception) }
            .collect { values ->
                _curatedPhotosResponse.value = values
            }
    }
}

sealed class UiAction {
    object Retry : UiAction()
    object Refresh : UiAction()
}

data class UiState(
    val photosList: List<Photo> = emptyList(),
    val error: Exception? = null,
    val loading: Boolean = false
)