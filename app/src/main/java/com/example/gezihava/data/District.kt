
package com.example.gezihava.model

data class District(
    val id: Int,
    val name: String,
    val provinceId: Int,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
