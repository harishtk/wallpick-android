package com.harishtk.app.wallpick.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.pexels.api.data.entity.Photo
import com.pexels.api.data.entity.Src

data class PhotoModel(
    @Embedded   val photo: Photo,
    @Relation(
        parentColumn = "id",
        entityColumn = "photo_id"
    )
    val src: Src
)