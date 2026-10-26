
package com.example.gezihava.data

import com.example.gezihava.model.District
import retrofit2.Response
import retrofit2.http.GET

interface DistrictApi {

    @GET("v2/datasets/districts.json")
    suspend fun getDistricts(): Response<List<District>>
}
