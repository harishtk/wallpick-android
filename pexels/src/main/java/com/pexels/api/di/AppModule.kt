package com.pexels.api.di

import android.content.Context
import com.pexels.api.data.repository.WallpaperRepositoryImpl
import com.pexels.api.data.source.local.WallpaperDatabase
import com.pexels.api.domain.repository.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideWallpaperDatabase(@ApplicationContext context: Context): WallpaperDatabase =
        WallpaperDatabase.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
interface AppBinderModule {

    @Binds
    fun bindsWallpaperRepository(
        wallpaperRepositoryImpl: WallpaperRepositoryImpl
    ): WallpaperRepository
}