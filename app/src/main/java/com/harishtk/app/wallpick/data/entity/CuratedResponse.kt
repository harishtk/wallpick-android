package com.harishtk.app.wallpick.data.entity

import com.squareup.moshi.Json

data class CuratedResponse(
    @field:Json(name = "page")  val page: Int,
    @field:Json(name = "per_page")  val perPage: Int,
    @field:Json(name = "total_results")  val totalResults: Long,
    @field:Json(name = "next_page")  val nextPage: String,
    @field:Json(name = "photos") val photos: List<Photo>
)