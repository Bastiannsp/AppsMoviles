package com.example.gamezone.repository

import com.example.gamezone.model.GameDeal

interface GameDealsRepository {
    suspend fun fetchTopDeals(limit: Int = 6): Result<List<GameDeal>>
}
