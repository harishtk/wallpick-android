package com.pexels.api.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pexels.api.data.source.remote.PhotosPagingSource
import com.pexels.api.data.source.remote.RemoteDataSource
import com.pexels.api.data.Result
import com.pexels.api.data.entity.Photo
import com.pexels.api.data.entity.PhotosResponse
import com.pexels.api.data.entity.Src
import com.pexels.api.data.source.BaseDataSource
import com.pexels.api.data.source.local.WallpaperDatabase
import com.pexels.api.data.source.remote.PexelsService
import com.pexels.api.di.IODispatcher
import com.pexels.api.domain.repository.WallpaperRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/* TODO: extract service, and house-keeping */
@Singleton
class WallpaperRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val service: PexelsService,
    private val database: WallpaperDatabase,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : WallpaperRepository, BaseDataSource() {

    override fun getCuratedStream(page: Int): Flow<Result<PhotosResponse>> = flow<Result<PhotosResponse>> {
        emit(Result.Loading)
        emit(getResult { remoteDataSource.getCuratedCall(page) })
    }.flowOn(ioDispatcher)

    override fun getSearchPhotos(query: String): Flow<PagingData<Photo>> {
        Timber.d("New query: $query")
        return Pager(
            config = PagingConfig(
                pageSize = PexelsService.DEFAULT_PER_PAGE_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PhotosPagingSource(this, service, query) }
        ).flow
    }

    override fun getPhotoById(id: Int): Flow<Result<Photo>> = flow {
        emit(Result.Loading)
        // TODO: This statement doesn't look good.  Works anyways
        emit(getResult { remoteDataSource.getPhoto(id = id) })
    }.flowOn(ioDispatcher)

    override fun getFavoritePhotos(): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = PexelsService.DEFAULT_PER_PAGE_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { database.favoritesDao().getAllPhotos() }
        ).flow
    }

    override fun getPhotoSrc(photoId: Long): Flow<Src> {
        return database.photoSrcDao().getSrcForPhoto(photoId = photoId)
    }

    override suspend fun addToFavorites(photo: Photo) = withContext(ioDispatcher) {
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
        Unit
    }
}