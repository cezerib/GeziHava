
package com.example.gezihava.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProvinceApi {

    @GET("provinces")
    suspend fun getProvinces(
        @Query("fields") fields: String = "id,name"
    ): Response<ProvinceResponse>


    @GET("districts")
    suspend fun getDistricts(
        @Query("provinceId") provinceId: Int,
        @Query("fields") fields: String = "id,name,provinceId"
    ): Response<DistrictResponse>

}

data class ProvinceResponse(
    val data: List<ProvinceItem>?
)

data class ProvinceItem(
    val id: Int,
    val name: String
)

data class DistrictResponse(
    val data: List<DistrictItem>?
)

data class DistrictItem(
    val id: Int,
    val name: String,
    @SerializedName("provinceId")
    val provinceId: Int? = null
)
