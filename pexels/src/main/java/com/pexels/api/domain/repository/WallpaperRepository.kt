package com.pexels.api.domain.repository

import androidx.paging.PagingData
import com.pexels.api.data.Result
import com.pexels.api.data.entity.Photo
import com.pexels.api.data.entity.PhotosResponse
import com.pexels.api.data.entity.Src
import kotlinx.coroutines.flow.Flow


interface WallpaperRepository {

    fun getCuratedStream(page: Int): Flow<Result<PhotosResponse>>

    fun getSearchPhotos(query: String): Flow<PagingData<Photo>>

    fun getPhotoById(id: Int): Flow<Result<Photo>>

    fun getFavoritePhotos(): Flow<PagingData<Photo>>

    fun getPhotoSrc(photoId: Long): Flow<Src>

    suspend fun addToFavorites(photo: Photo)
}