package com.harishtk.app.wallpick.data.source.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PhotosPagingSource(
    private val repository: WallpaperRepository,
    private val pexelsService: PexelsService,
    private val query: String
) : PagingSource<Int, Photo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val position = params.key ?: PHOTOS_PAGING_STARTING_INDEX
        val apiQuery = query
        return try {
            // TODO: move to repository
            val response = pexelsService.searchPhotos(apiQuery, position, params.loadSize)
            val photos = response.photos
            val nextKey = if (photos.isEmpty()) {
                null
            } else {
                // initial load size = 3 * DEFAULT_PER_PAGE_LIMIT
                // ensure we're not requesting duplicating items, at the 2nd request
                position + (params.loadSize / PexelsService.DEFAULT_PER_PAGE_LIMIT)
            }
            LoadResult.Page(
                data = photos,
                prevKey = if (position == PHOTOS_PAGING_STARTING_INDEX) null else position -1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    }

}

const val PHOTOS_PAGING_STARTING_INDEX = 1