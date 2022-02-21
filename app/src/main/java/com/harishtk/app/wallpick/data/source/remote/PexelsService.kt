package com.harishtk.app.wallpick.data.source.remote

import com.harishtk.app.wallpick.BuildConfig
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.entity.PhotosResponse
import com.harishtk.app.wallpick.data.entity.Video
import com.harishtk.app.wallpick.data.entity.VideosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

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

    @GET("/v1/photos/{id}")
    suspend fun getPhoto(
        @Path(value = "id", encoded = true) id: String
    ): Response<Photo>

    @GET("/videos/search")
    suspend fun searchVideos(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE_LIMIT,
    ): VideosResponse

    @GET("/videos/popular")
    suspend fun popularVideos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE_LIMIT
    ): VideosResponse

    @GET("/videos/{id}")
    suspend fun getVideo(
        @Path(value = "id", encoded = true) id: String
    ): Video

    companion object {
        const val BASE_URL       = "https://api.pexels.com"
        private const val BASE_URL_PHOTO = "https://api.pexels.com/v1"
        private const val BASE_URL_VIDEO = "https://api.pexels.com/videos"

        const val DEFAULT_PER_PAGE_LIMIT = 15

        val apiKey = BuildConfig.SecureProps.getOrDefault("PEXELS_API_KEY", "")

        const val HEADER_X_RATE_LIMIT = "X-Ratelimit-Limit"
        const val HEADER_X_RATE_LIMIT_REMAINING = "X-Ratelimit-Remaining"
        const val HEADER_X_RATE_LIMIT_RESET = "X-Ratelimit-Reset"
    }
}