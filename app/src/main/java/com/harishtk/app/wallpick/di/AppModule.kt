package com.harishtk.app.wallpick.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harishtk.app.wallpick.data.source.remote.ApiService
import com.harishtk.app.wallpick.data.source.remote.PexelsService
import com.harishtk.app.wallpick.net.HeaderInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(
        gson: Gson,
        moshi: Moshi
    ): Retrofit {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val authHeaderInterceptor = HeaderInterceptor("Authorization", PexelsService.apiKey)

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authHeaderInterceptor)

        return Retrofit.Builder()
            .baseUrl(PexelsService.BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideMoshi() = Moshi.Builder().build()

    @Provides
    fun provideGson() = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Singleton
    @Provides
    fun providePexelsService(retrofit: Retrofit): PexelsService = retrofit.create(PexelsService::class.java)
}