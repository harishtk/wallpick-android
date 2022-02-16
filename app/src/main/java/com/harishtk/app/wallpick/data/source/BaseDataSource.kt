package com.harishtk.app.wallpick.data.source

import androidx.annotation.WorkerThread
import com.harishtk.app.wallpick.utils.BadResponseException
import retrofit2.Response
import com.harishtk.app.wallpick.data.Result

abstract class BaseDataSource {

    @WorkerThread
    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    throw BadResponseException("Empty body.")
                }
            } else {
                throw BadResponseException("Unexpected response code: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}