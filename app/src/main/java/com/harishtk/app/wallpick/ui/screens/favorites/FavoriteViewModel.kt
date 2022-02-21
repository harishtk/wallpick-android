package com.harishtk.app.wallpick.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    val pagingFavoritePhotosFlow: Flow<PagingData<Photo>> = repository.getFavoritePhotos()
        .map { pagingData: PagingData<Photo> ->
            pagingData.map { photo ->
                try {
                    photo.src = repository.getPhotoSrc(photo.id).first()
                    photo
                } catch (e: IOException) {
                    photo
                }
            }
        }
        .cachedIn(viewModelScope)

}