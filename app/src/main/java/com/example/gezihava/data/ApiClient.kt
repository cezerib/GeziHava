
package com.example.gezihava.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {

    // Hava durumu API'si: Open-Meteo
    private val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApi: WeatherApi by lazy {
        weatherRetrofit.create(WeatherApi::class.java)
    }

    // Türkiye illeri API'si
    private val provinceRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.turkiyeapi.dev/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val provinceApi: ProvinceApi by lazy {
        provinceRetrofit.create(ProvinceApi::class.java)
    }

    // Türkiye ilçeleri API'si
    private val districtRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.turkiyeapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val districtApi: DistrictApi by lazy {
        districtRetrofit.create(DistrictApi::class.java)
    }

    // Turistik yerler API'si: Türkçe Wikipedia

    private val touristPlaceRetrofit: Retrofit by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header(
                        "User-Agent",
                        "GeziHava/1.0 (Android; tourist weather app)"
                    )
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://tr.wikipedia.org/")
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val touristPlaceApi: TouristPlaceApi by lazy {
        touristPlaceRetrofit.create(
            TouristPlaceApi::class.java
        )
    }

}
