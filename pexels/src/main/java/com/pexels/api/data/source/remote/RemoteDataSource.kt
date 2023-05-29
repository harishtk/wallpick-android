package com.pexels.api.data.source.remote

import com.pexels.api.data.source.BaseDataSource
import com.pexels.api.data.source.WallpaperDataSource
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val pexelsService: PexelsService
) : BaseDataSource(), WallpaperDataSource {

    suspend fun getCurated(
        page: Int
    ) = getResult { pexelsService.getCurated(page = page) }

    suspend fun getCuratedCall(
        page: Int
    ) = pexelsService.getCurated(page = page)

    suspend fun getPhoto(
        id: Int
    ) = pexelsService.getPhoto(id.toString())
}