package com.harishtk.app.wallpick.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harishtk.app.wallpick.data.source.local.WallpaperDatabase
import com.harishtk.app.wallpick.data.source.remote.ApiService
import com.harishtk.app.wallpick.data.source.remote.PexelsService
import com.harishtk.app.wallpick.net.HeaderInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun providesWallpaperDatabase(@ApplicationContext context: Context) =
        WallpaperDatabase.getInstance(context = context)
}