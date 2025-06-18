package com.example.weather.api

sealed class NetworkResponse<out T> {
//    T is WeatheModel
    data class Success<out T>(val data: T) : NetworkResponse<T>()
    data class Error(val message: String) : NetworkResponse<Nothing>()
    object Loading : NetworkResponse<Nothing>()
}