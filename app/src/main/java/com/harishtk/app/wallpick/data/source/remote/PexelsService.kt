package com.harishtk.app.wallpick.data.source.remote

import com.harishtk.app.wallpick.BuildConfig
import com.harishtk.app.wallpick.data.entity.PhotosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsService {

    @GET("/v1/curated")
    suspend fun getCurated(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE_LIMIT
    ): Response<PhotosResponse>

    @GET("/v1/search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE_LIMIT,
    ): PhotosResponse

    companion object {
        const val BASE_URL       = "https://api.pexels.com"
        private const val BASE_URL_PHOTO = "https://api.pexels.com/v1"
        private const val BASE_URL_VIDEO = "https://api.pexels.com/videos"

        const val DEFAULT_PER_PAGE_LIMIT = 15

        val apiKey = BuildConfig.SecureProps.getOrDefault("PEXELS_API_KEY", "")
    }
}