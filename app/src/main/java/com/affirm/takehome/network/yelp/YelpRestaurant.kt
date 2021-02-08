package com.affirm.takehome.network.yelp

import com.affirm.takehome.data.Restaurant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class YelpRestaurant(
    @SerialName("id") override val id: String,
    @SerialName("name") override val name: String,
    @SerialName("image_url") override val image: String,
    @SerialName("rating") override val rating: String
) : Restaurant(id, name, image, rating)