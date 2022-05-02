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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<UiState>

    val pagingDataFlow: Flow<PagingData<Photo>>

    val accept: (UiAction) -> Unit

    val totalResults = MutableStateFlow(0)

    init {

        val lastQuery = savedStateHandle[LAST_SEARCH_QUERY] ?: DEFAULT_QUERY
        val lastQueryScrolled = savedStateHandle[LAST_QUERY_SCROLLED] ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .debounce(300)
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

        val totalResults = actionStateFlow
            .filterIsInstance<UiAction.UpdateTotalResults>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.UpdateTotalResults(totalResults = 0)) }

        val favorited = actionStateFlow
            .filterIsInstance<UiAction.Favorite>()
            .map { uiAction -> addToFavorite(photo = uiAction.photo) }

        pagingDataFlow = searches
            .flatMapLatest {
                searchRepo(queryString = it.query)
                /*if (it.query.isEmpty()) {

                } else {
                }*/
            }
            .cachedIn(viewModelScope)

        uiState = combine(
            searches,
            queriesScrolled,
            totalResults,
            ::Triple
        ).map { (search, scroll, totalResults) ->
            Timber.d("Total results: $totalResults")
            UiState(
                query = search.query,
                lastQueryScrolled = scroll.currentQuery,
                hasNotScrolledForCurrentSearch = search.query != scroll.currentQuery,
                totalResults = totalResults.totalResults
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action ->
            Timber.d("$action")
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    fun addToFavorite(photo: Photo) {
        viewModelScope.launch { repository.addToFavorites(photo) }
    }

    private fun searchRepo(queryString: String): Flow<PagingData<Photo>> =
        repository.getSearchPhotos(queryString).map {
            it.map { photo -> accept(UiAction.UpdateTotalResults(totalResults = photo.totalResults));photo }
        }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
        savedStateHandle[LAST_QUERY_SCROLLED] = uiState.value.lastQueryScrolled
        super.onCleared()
    }
}

sealed class UiAction {
    data class Search(val query: String) : UiAction()
    data class Scroll(val currentQuery: String) : UiAction()
    data class Favorite(val photo: Photo): UiAction()
    data class UpdateTotalResults(val totalResults: Int): UiAction()
    object Retry : UiAction()
    object Refresh : UiAction()
}

data class UiState(
    val error: Exception? = null,
    val loading: Boolean = false,
    val query: String = DEFAULT_QUERY,
    val lastQueryScrolled: String = DEFAULT_QUERY,
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val totalResults: Int = 0,
)

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val LAST_QUERY_SCROLLED: String = "last_query_scrolled"
private const val DEFAULT_QUERY = "Dogs"