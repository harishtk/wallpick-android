package com.pexels.api.di

import com.harishtk.app.wallpick.net.HeaderInterceptor
import com.pexels.api.data.source.remote.PexelsService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkhttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val authHeaderInterceptor = HeaderInterceptor("Authorization", PexelsService.apiKey)

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authHeaderInterceptor)
        return clientBuilder.build()
    }

    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PexelsService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Singleton
    @Provides
    fun providePexelsService(retrofit: Retrofit): PexelsService = retrofit.create(PexelsService::class.java)
}