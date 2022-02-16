package com.harishtk.app.wallpick

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import com.harishtk.app.wallpick.data.source.respository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    private var _curatedPhotos = wallpaperRepository.getCurated(1)
    val curatedPhotos: LiveData<Result<CuratedResponse>> = _curatedPhotos
}