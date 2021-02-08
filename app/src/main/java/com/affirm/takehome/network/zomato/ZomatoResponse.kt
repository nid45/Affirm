package com.affirm.takehome.network.zomato

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZomatoResponse (
    @SerialName("restaurants") val restaurants: List<ZomatoRestaurant> = listOf()
)