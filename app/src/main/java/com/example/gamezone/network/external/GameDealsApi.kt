package com.example.gamezone.network.external

import com.example.gamezone.network.external.dto.GameDealDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GameDealsApi {

    @GET("api/1.0/deals")
    suspend fun getDeals(
        @Query("storeID") storeId: Int = 1,
        @Query("sortBy") sortBy: String = "Deal Rating",
        @Query("pageSize") pageSize: Int = 6,
        @Query("upperPrice") upperPrice: Double = 45.0
    ): List<GameDealDto>
}
