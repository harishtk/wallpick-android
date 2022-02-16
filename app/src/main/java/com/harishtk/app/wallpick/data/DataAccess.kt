package com.harishtk.app.wallpick.data

import androidx.lifecycle.liveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun <T> performNetworkCall(
    workContext: CoroutineDispatcher = Dispatchers.IO,
    networkCall: suspend () -> Result<T>
) = liveData(workContext) {
    emit(Result.Loading)
    val responseStatus = networkCall()
    if (responseStatus.succeeded) {
        emit(responseStatus)
    } else {
        emit(Result.Error((responseStatus as Result.Error).exception))
    }
}