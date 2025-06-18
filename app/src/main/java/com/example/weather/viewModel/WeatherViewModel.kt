package com.example.weather.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.api.Constant
import com.example.weather.api.NetworkResponse
import com.example.weather.api.RetrofitInstance
import com.example.weather.api.WeatherModel
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val weatherApi = RetrofitInstance.weatherApi
    private val _weatheResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatheResult: LiveData<NetworkResponse<WeatherModel>> = _weatheResult

    fun getData(city: String) {
        _weatheResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(Constant.apikey, city)
                if (response.isSuccessful) {
                    response?.body()?.let {
                        _weatheResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    _weatheResult.value = NetworkResponse.Error("Failed To fetch the Data")
                }
            } catch (e: Exception) {
                _weatheResult.value = NetworkResponse.Error("Failed To fetch the Data")
            }
        }
    }
}