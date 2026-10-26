
package com.example.gezihava.data

import com.example.gezihava.model.City
import com.example.gezihava.model.District
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

private data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

private data class GeocodingResult(
    val name: String?,
    val latitude: Double,
    val longitude: Double,

    @SerializedName("country_code")
    val countryCode: String?
)

class CityRepository {

    private val geocodingApi: GeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApi::class.java)
    }

    // Türkiye illerini API'den getir.
    suspend fun getCities(): Result<List<City>> {
        return try {
            val response = ApiClient.provinceApi.getProvinces()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("İl listesi alınamadı: HTTP ${response.code()}")
                )
            }

            val provinces = response.body()?.data
                ?: return Result.failure(Exception("API boş veri döndürdü."))

            Result.success(
                provinces.map { province ->
                    City(
                        id = province.id,
                        name = province.name,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Seçilen ilin koordinatlarını bul.
    suspend fun getCityCoordinates(city: City): Result<City> {
        return try {
            val response = geocodingApi.searchCity(
                name = "${city.name}, Türkiye"
            )

            val location = response.results
                ?.firstOrNull { it.countryCode == "TR" }
                ?: return Result.failure(
                    Exception("${city.name} için Türkiye'de koordinat bulunamadı.")
                )

            Result.success(
                city.copy(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Seçilen ile ait ilçeleri API'den getir.
    suspend fun getDistricts(provinceId: Int): Result<List<District>> {
        return try {
            val response = ApiClient.districtApi.getDistricts()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("İlçeler alınamadı: HTTP ${response.code()}")
                )
            }

            val districts = response.body()
                ?: return Result.failure(Exception("İlçe verisi boş döndü."))

            val filteredDistricts = districts
                .filter { it.provinceId == provinceId }
                .sortedBy { it.name }

            if (filteredDistricts.isEmpty()) {
                return Result.failure(
                    Exception("Bu ile ait ilçe bulunamadı. İl kimliğini kontrol et.")
                )
            }

            Result.success(filteredDistricts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // İlçe koordinatlarını, ilçe ve il adını birlikte arayarak bul.
    suspend fun getDistrictCoordinates(
        district: District,
        provinceName: String
    ): Result<District> {
        return try {
            val response = geocodingApi.searchCity(
                name = "${district.name}, $provinceName, Türkiye"
            )

            val location = response.results
                ?.firstOrNull { it.countryCode == "TR" }
                ?: return Result.failure(
                    Exception(
                        "${district.name}, $provinceName için koordinat bulunamadı."
                    )
                )

            Result.success(
                district.copy(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
