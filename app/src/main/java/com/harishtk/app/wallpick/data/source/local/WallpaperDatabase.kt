package com.harishtk.app.wallpick.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harishtk.app.wallpick.data.entity.Photo
import dagger.hilt.android.qualifiers.ApplicationContext

@Database(
    entities = [Photo::class],
    version = 1,
    exportSchema = false
)
abstract class WallpaperDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) { INSTANCE ?: builDatabase(context).also { INSTANCE = it } }

        private fun builDatabase(@ApplicationContext context: Context) =
            Room.databaseBuilder(context.applicationContext, WallpaperDatabase::class.java, "Wallpaper.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}