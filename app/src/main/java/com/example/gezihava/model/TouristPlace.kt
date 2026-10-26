
package com.example.gezihava.model

data class TouristPlace(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = "Diğer",
    val cityName: String = "",
    val districtName: String = "",
    val description: String = ""
)
