package com.harishtk.app.wallpick.data.source.remote

import com.harishtk.app.wallpick.data.source.BaseDataSource
import com.harishtk.app.wallpick.data.source.WallpaperDataSource
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
}