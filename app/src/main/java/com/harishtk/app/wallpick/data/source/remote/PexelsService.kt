package com.harishtk.app.wallpick.data.source.remote

import com.harishtk.app.wallpick.BuildConfig
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface PexelsService {

    @GET("/v1/curated")
    suspend fun getCurated(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE_LIMIT
    ): Response<CuratedResponse>

    companion object {
        const val BASE_URL       = "https://api.pexels.com"
        private const val BASE_URL_PHOTO = "https://api.pexels.com/v1"
        private const val BASE_URL_VIDEO = "https://api.pexels.com/videos"

        private const val DEFAULT_PER_PAGE_LIMIT = 10

        val apiKey = BuildConfig.SecureProps.getOrDefault("PEXELS_API_KEY", "")
    }
}