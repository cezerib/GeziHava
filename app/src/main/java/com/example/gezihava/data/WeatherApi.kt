
package com.example.gezihava.data

import com.example.gezihava.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m",
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): Response<WeatherResponse>
}
