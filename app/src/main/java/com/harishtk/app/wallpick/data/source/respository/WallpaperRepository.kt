package com.harishtk.app.wallpick.data.source.respository

import androidx.lifecycle.LiveData
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import com.harishtk.app.wallpick.data.performNetworkCall
import com.harishtk.app.wallpick.data.source.BaseDataSource
import com.harishtk.app.wallpick.data.source.remote.RemoteDataSource
import com.harishtk.app.wallpick.di.IODispatcher
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class WallpaperRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @IODispatcher private val workDispatcher: CoroutineDispatcher
) : BaseDataSource() {

    fun getCurated(page: Int): LiveData<Result<CuratedResponse>> =
        performNetworkCall(workDispatcher) { remoteDataSource.getCurated(page) }

    fun getCuratedFlow(page: Int): Flow<Result<CuratedResponse>> = flow<Result<CuratedResponse>> {
        emit(Result.Loading)
        delay(2000)
        emit(getResult { remoteDataSource.getCuratedCall(page) })
    }.flowOn(workDispatcher)
}