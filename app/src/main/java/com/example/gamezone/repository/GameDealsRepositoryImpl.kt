package com.example.gamezone.repository

import com.example.gamezone.model.GameDeal
import com.example.gamezone.network.external.GameDealsApi
import com.example.gamezone.network.external.dto.toDomain
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameDealsRepositoryImpl(
    private val api: GameDealsApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GameDealsRepository {

    override suspend fun fetchTopDeals(limit: Int): Result<List<GameDeal>> = withContext(ioDispatcher) {
        return@withContext try {
            val deals = api.getDeals(pageSize = limit)
                .mapNotNull { dto -> dto.toDomain() }
            Result.success(deals)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            val message = when (exception) {
                is IOException -> "No se pudo conectar al servicio de ofertas"
                else -> exception.message ?: "Ocurrió un error inesperado al obtener las ofertas"
            }
            Result.failure(IllegalStateException(message, exception))
        }
    }
}
