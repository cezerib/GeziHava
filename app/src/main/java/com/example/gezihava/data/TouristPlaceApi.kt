
package com.example.gezihava.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TouristPlaceApi {

    @GET("w/api.php")
    suspend fun searchNearbyPlaces(
        @Query("action") action: String = "query",
        @Query("list") list: String = "geosearch",
        @Query("gscoord") coordinates: String,
        @Query("gsradius") radiusMeters: Int = 10000,
        @Query("gslimit") limit: Int = 20,
        @Query("format") format: String = "json"
    ): Map<String, Any>
}
