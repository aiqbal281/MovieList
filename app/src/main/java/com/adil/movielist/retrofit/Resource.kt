package com.adil.movielist.retrofit

sealed class Resource<out T> {

    class Loading<T> : Resource<T>()

    data class Success<T>(val data: T) : Resource<T>()

    data class Error<T>(val message: String) : Resource<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> error(message: String) = Error<T>(message)
    }
}
