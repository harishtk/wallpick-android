package com.harishtk.app.wallpick.data.source.respository

import androidx.lifecycle.LiveData
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import com.harishtk.app.wallpick.data.performNetworkCall
import com.harishtk.app.wallpick.data.source.remote.RemoteDataSource
import com.harishtk.app.wallpick.di.IODispatcher
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@ViewModelScoped
class WallpaperRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @IODispatcher private val workContext: CoroutineDispatcher
) {

    fun getCurated(page: Int): LiveData<Result<CuratedResponse>> =
        performNetworkCall(workContext) { remoteDataSource.getCurated(page) }

}