
package com.example.gezihava.data

import retrofit2.http.GET

interface CityApi {

    @GET("v1/forecast")
    suspend fun getCityWeather(
        @retrofit2.http.Query("latitude") latitude: Double,
        @retrofit2.http.Query("longitude") longitude: Double,
        @retrofit2.http.Query("current")
        current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m",
        @retrofit2.http.Query("daily")
        daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @retrofit2.http.Query("timezone")
        timezone: String = "auto"
    ): retrofit2.Response<okhttp3.ResponseBody>
}
