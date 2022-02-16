package com.harishtk.app.wallpick.net

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val headerName: String,
    private val headerValue: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder().apply {
                addHeader(headerName, headerValue)
            }.build()
        )
    }

}