package com.harishtk.app.wallpick.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harishtk.app.wallpick.data.entity.Src
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoSrcDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(src: List<Src>)

    @Query("SELECT * FROM src WHERE photo_id = :photoId")
    fun getSrcForPhoto(photoId: Long): Flow<Src>

    @Query("DELETE FROM src")
    suspend fun deleteAllSrc()
}