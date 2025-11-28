package com.example.gamezone.network.external.dto

import com.example.gamezone.model.GameDeal

/**
 * Representa una oferta individual según el esquema entregado por CheapShark.
 */
data class GameDealDto(
    val title: String?,
    val salePrice: String?,
    val normalPrice: String?,
    val savings: String?,
    val dealRating: String?,
    val thumb: String?
)

fun GameDealDto.toDomain(): GameDeal? {
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val sale = salePrice?.toDoubleOrNull() ?: return null
    val normal = normalPrice?.toDoubleOrNull() ?: sale
    val savingsPercent = savings?.toDoubleOrNull() ?: computeSavings(normal, sale)
    val rating = dealRating?.toDoubleOrNull()
    val thumbnail = thumb?.takeIf { it.isNotBlank() }

    return GameDeal(
        title = safeTitle,
        salePrice = sale,
        normalPrice = normal,
        savingsPercentage = savingsPercent,
        dealRating = rating,
        thumbnailUrl = thumbnail
    )
}

private fun computeSavings(normalPrice: Double, salePrice: Double): Double {
    if (normalPrice <= 0.0) return 0.0
    val diff = normalPrice - salePrice
    if (diff <= 0.0) return 0.0
    return (diff / normalPrice) * 100.0
}
