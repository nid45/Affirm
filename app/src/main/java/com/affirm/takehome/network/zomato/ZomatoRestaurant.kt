package com.affirm.takehome.network.zomato

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ZomatoRestaurant(
    @SerialName("restaurant") val restaurantDetail: ZomatoRestaurantDetail
)

@Serializable
class ZomatoRestaurantDetail (
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String,
    @SerialName("featured_image") val image: String,
    @SerialName("user_rating") val userRating: UserRating
)

@Serializable
data class UserRating(
    @SerialName("aggregate_rating") val rating: String
)
