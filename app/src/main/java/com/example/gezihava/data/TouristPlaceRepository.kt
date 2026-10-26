
package com.example.gezihava.data

import com.example.gezihava.model.TouristPlace

class TouristPlaceRepository {

    suspend fun getTouristPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 3000
    ): Result<List<TouristPlace>> {
        return try {
            require(latitude in -90.0..90.0) {
                "Enlem değeri geçersiz."
            }
            require(longitude in -180.0..180.0) {
                "Boylam değeri geçersiz."
            }

            val radius = radiusMeters.coerceIn(1000, 10000)

            val response = ApiClient.touristPlaceApi.searchNearbyPlaces(
                coordinates = "$latitude|$longitude",
                radiusMeters = radius,
                limit = 20
            )

            val query = response["query"] as? Map<*, *>
                ?: return Result.failure(
                    Exception("Wikipedia arama sonucu döndürmedi.")
                )

            val results = query["geosearch"] as? List<*>
                ?: return Result.success(emptyList())

            val places = results.mapNotNull { item ->
                val page = item as? Map<*, *>
                    ?: return@mapNotNull null

                val id = (page["pageid"] as? Number)?.toLong()
                    ?: return@mapNotNull null

                val name = page["title"] as? String
                    ?: return@mapNotNull null

                val lat = (page["lat"] as? Number)?.toDouble()
                    ?: return@mapNotNull null

                val lon = (page["lon"] as? Number)?.toDouble()
                    ?: return@mapNotNull null

                TouristPlace(
                    id = id,
                    name = name,
                    latitude = lat,
                    longitude = lon,
                    category = "Turistik / tarihî yer",
                    description = ""
                )
            }.distinctBy { it.id }
                .sortedBy { it.name }

            Result.success(places)

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.message ?: "Wikipedia API isteği başarısız oldu.",
                    e
                )
            )
        }
    }
}
