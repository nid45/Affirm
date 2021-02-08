package com.affirm.takehome.network.zomato

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Note: You could use this or define your own.
interface ZomatoRestaurantService {
    @GET("search")
    fun getZomatoRestaurants(@Header(value = "user-key") token: String,
                             @Query("lat") latitude: Double,
                             @Query("lon") longitude: Double,
                             @Query("start") offset: Int = 0): Call<ZomatoResponse>
}