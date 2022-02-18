package com.harishtk.app.wallpick.data.source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harishtk.app.wallpick.data.entity.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): PagingSource<Int, Photo>

    @Query("DELETE FROM photos")
    suspend fun deleteFavorites()
}