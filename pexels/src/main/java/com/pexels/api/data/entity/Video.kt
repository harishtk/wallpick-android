package com.pexels.api.data.entity

import com.squareup.moshi.Json

data class Video(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "width") val width: Int,
    @field:Json(name = "height") val height: Int,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "image") val image: String,
    @field:Json(name = "full_res") val fullRes: String,
    @field:Json(name = "tags") val tags: List<Any>,
    @field:Json(name = "duration") val duration: Int,
    @field:Json(name = "video_files") val videoFiles: List<VideoFile>,
    @field:Json(name = "video_pictures") val videoPictures: List<VideoPicture>
)

data class User(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "url") val url: String
)

data class VideoFile(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "width") val width: Int,
    @field:Json(name = "height") val height: Int,
    @field:Json(name = "quality") val quality: String,
    @field:Json(name = "file_type") val fileType: String,
    @field:Json(name = "link") val link: String

)

data class VideoPicture(
    @field:Json(name = "id") val id: Int,
    @field:Json(name = "picture") val picture: String,
    @field:Json(name = "nr") val nr: Int
)