package com.pexels.api.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pexels.api.data.entity.Photo
import com.pexels.api.data.entity.Src
import dagger.hilt.android.qualifiers.ApplicationContext

@Database(
    entities = [Photo::class, Src::class],
    version = 1,
    exportSchema = false
)
abstract class WallpaperDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao
    abstract fun photoSrcDao(): PhotoSrcDao

    companion object {
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(@ApplicationContext context: Context) =
            Room.databaseBuilder(context.applicationContext, WallpaperDatabase::class.java, "Wallpaper.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}