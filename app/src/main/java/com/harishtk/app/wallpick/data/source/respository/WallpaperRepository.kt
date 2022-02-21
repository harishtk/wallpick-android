package com.harishtk.app.wallpick.data.source.respository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.entity.PhotosResponse
import com.harishtk.app.wallpick.data.entity.Src
import com.harishtk.app.wallpick.data.performNetworkCall
import com.harishtk.app.wallpick.data.source.BaseDataSource
import com.harishtk.app.wallpick.data.source.local.WallpaperDatabase
import com.harishtk.app.wallpick.data.source.remote.PexelsService
import com.harishtk.app.wallpick.data.source.remote.PhotosPagingSource
import com.harishtk.app.wallpick.data.source.remote.RemoteDataSource
import com.harishtk.app.wallpick.di.IODispatcher
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/* TODO: extract service, and house-keeping */
@ActivityRetainedScoped
class WallpaperRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val service: PexelsService,
    private val database: WallpaperDatabase,
    @IODispatcher private val workDispatcher: CoroutineDispatcher
) : BaseDataSource() {

    fun getCurated(page: Int): LiveData<Result<PhotosResponse>> =
        performNetworkCall(workDispatcher) { remoteDataSource.getCurated(page) }

    fun getCuratedFlow(page: Int): Flow<Result<PhotosResponse>> = flow<Result<PhotosResponse>> {
        emit(Result.Loading)
        delay(2000)
        emit(getResult { remoteDataSource.getCuratedCall(page) })
    }.flowOn(workDispatcher)

    fun getSearchPhotos(query: String): Flow<PagingData<Photo>> {
        Timber.d("New query: $query")
        return Pager(
            config = PagingConfig(
                pageSize = PexelsService.DEFAULT_PER_PAGE_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PhotosPagingSource(this, service, query) }
        ).flow
    }

    fun getPhotoById(id: Int): Flow<Result<Photo>> = flow {
        emit(Result.Loading)
        // TODO: This statement doesn't look good.  Works anyways
        emit(getResult { remoteDataSource.getPhoto(id = id) })
    }.flowOn(workDispatcher)

    fun getFavoritePhotos(): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = PexelsService.DEFAULT_PER_PAGE_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { database.favoritesDao().getAllPhotos() }
        ).flow
    }

    fun getPhotoSrc(photoId: Long): Flow<Src> {
        return database.photoSrcDao().getSrcForPhoto(photoId = photoId)
    }

    suspend fun addToFavorites(photo: Photo) = withContext(workDispatcher) {
        launch {
            kotlin.runCatching {
                Timber.d("Favorites: ADD $photo")
                database.favoritesDao().insertAll(photos = listOf(photo))
                photo.src?.let { photoSrc ->
                    photoSrc.photoId = photo.id
                    database.photoSrcDao().insertAll(listOf(photoSrc))
                }
            }.exceptionOrNull()?.let {
                Timber.e(it, "Failed to add to favorites")
            }
        }
    }
}