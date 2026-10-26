package com.example.gezihava.data

import com.example.gezihava.model.WeatherResponse

class WeatherRepository {

    private val api = ApiClient.weatherApi

    suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ): Result<WeatherResponse> {
        return try {
            val response = api.getWeather(
                latitude = latitude,
                longitude = longitude
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception("Hava durumu verisi boş geldi.")
                    )
                }
            } else {
                Result.failure(
                    Exception("API hatası: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
