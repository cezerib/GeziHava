
package com.example.gezihava.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather?,
    val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double?,

    @SerializedName("relative_humidity_2m")
    val humidity: Int?,

    @SerializedName("apparent_temperature")
    val apparentTemperature: Double?,

    @SerializedName("precipitation")
    val precipitation: Double?,

    @SerializedName("weather_code")
    val weatherCode: Int?,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double?
)

data class DailyWeather(
    @SerializedName("time")
    val dates: List<String>?,

    @SerializedName("weather_code")
    val weatherCodes: List<Int>?,

    @SerializedName("temperature_2m_max")
    val maxTemperatures: List<Double>?,

    @SerializedName("temperature_2m_min")
    val minTemperatures: List<Double>?
)
