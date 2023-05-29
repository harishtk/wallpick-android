package com.pexels.api.data.entity

import com.pexels.api.data.entity.Video
import com.squareup.moshi.Json

data class VideosResponse(
    @field:Json(name = "page")  val page: Int,
    @field:Json(name = "per_page")  val perPage: Int,
    @field:Json(name = "total_results")  val totalResults: Long,
    @field:Json(name = "next_page")  val nextPage: String,
    @field:Json(name = "videos") val photos: List<Video>,
    @field:Json(name = "error") val error: String?,
    @field:Json(name = "url") val url: String,
)