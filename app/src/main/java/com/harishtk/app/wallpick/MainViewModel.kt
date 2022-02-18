package com.harishtk.app.wallpick

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.PhotosResponse
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import com.harishtk.app.wallpick.data.succeeded
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<UiState>

    val pagingDataFlow: Flow<PagingData<Photo>>

    val accept: (UiAction) -> Unit

    private var _photosPhotosResponse: MutableLiveData<Result<PhotosResponse>> = MutableLiveData()

    init {

        val lastQuery = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
        val lastQueryScrolled = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = lastQuery)) }
        val queriesScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(UiAction.Scroll(currentQuery = lastQueryScrolled)) }

        pagingDataFlow = searches
            .flatMapLatest { searchRepo(queryString = it.query) }
            .cachedIn(viewModelScope)

        uiState = combine(
            searches,
            queriesScrolled,
            ::Pair
        ).map { (search, scroll) ->
            UiState(
                query = search.query,
                lastQueryScrolled = scroll.currentQuery,
                hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        viewModelScope.launch {
            /*repository.getCuratedFlow(1)
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
                }*/
        }

        accept = { action ->
            Timber.d("$action")
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }
    val photosPhotosResponse: LiveData<Result<PhotosResponse>> = _photosPhotosResponse

    fun fetchCuratedPhotos() = viewModelScope.launch {
        repository.getCuratedFlow(1)
            .catch { exception -> Timber.e(exception) }
            .collect { values ->
                _photosPhotosResponse.value = values
            }
    }

    private fun searchRepo(queryString: String): Flow<PagingData<Photo>> =
        repository.getSearchPhotos(queryString)

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
        savedStateHandle[LAST_QUERY_SCROLLED] = uiState.value.lastQueryScrolled
        super.onCleared()
    }
}

sealed class UiAction {
    data class Search(val query: String) : UiAction()
    data class Scroll(val currentQuery: String) : UiAction()
    object Retry : UiAction()
    object Refresh : UiAction()
}

data class UiState(
    val error: Exception? = null,
    val loading: Boolean = false,
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false,
)

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val DEFAULT_QUERY = "Dogs"