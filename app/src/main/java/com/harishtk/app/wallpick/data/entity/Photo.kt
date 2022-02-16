package com.harishtk.app.wallpick.data.entity

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.Serializable

data class Photo(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "width") val width: Int,
    @field:Json(name = "height") val height: Int,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "photographer") val photographer: String,
    @field:Json(name = "photographer_url") val photographerUrl: String,
    @field:Json(name = "photographer_id") val photographerId: String,
    @field:Json(name = "avg_color") val avgColor: String,
    @field:Json(name = "liked") val liked: Boolean,
    @field:Json(name = "alt") val alt: String,
    @field:Json(name = "src") val src: Src
): Serializable {
    data class Src(
        @field:Json(name = "original") val original: String,
        @field:Json(name = "large2x") val large2x: String,
        @field:Json(name = "large") val large: String,
        @field:Json(name = "medium") val medium: String,
        @field:Json(name = "small") val small: String,
        @field:Json(name = "portrait") val portrait: String,
        @field:Json(name = "landscape") val landscape: String,
        @field:Json(name = "tiny") val tiny: String
    )
}