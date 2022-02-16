package com.harishtk.app.wallpick.data

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    enum class Status { SUCCESS, ERROR, LOADING }

    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data = data, message = null)
        }

        fun <T> error(message: String): Resource<T> {
            return Resource(Status.ERROR, data = null, message = message)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, data = null, message = null)
        }
    }
}