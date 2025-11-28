package com.example.gamezone.model

data class GameDeal(
    val title: String,
    val salePrice: Double,
    val normalPrice: Double,
    val savingsPercentage: Double,
    val dealRating: Double?,
    val thumbnailUrl: String?
)
