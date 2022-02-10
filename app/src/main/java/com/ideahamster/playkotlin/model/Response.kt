package com.ideahamster.playkotlin.model
sealed class Response<out T> {
    data class Success<out T>(val responseBody: T) : Response<T>()
    data class Error<out T>(val responseBody: T) : Response<T>()

    fun <R> transform(transform: (T) -> R): Response<R> {
        return when (this) {
            is Success -> Success(transform(responseBody))
            is Error -> Error(transform(responseBody))
        }
    }
}