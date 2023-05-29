package com.pexels.api.data.source.remote

import com.pexels.api.data.entity.Photo
import com.pexels.api.data.entity.PhotosResponse
import com.pexels.api.data.entity.Video
import com.pexels.api.data.entity.VideosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

        val apiKey = "563492ad6f91700001000001b88ba9fa24f640babab636ccead207a3"

        const val HEADER_X_RATE_LIMIT = "X-Ratelimit-Limit"
        const val HEADER_X_RATE_LIMIT_REMAINING = "X-Ratelimit-Remaining"
        const val HEADER_X_RATE_LIMIT_RESET = "X-Ratelimit-Reset"
    }
}