package com.example.gamezone.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class LocationTracker(context: Context) {

    private val appContext = context.applicationContext
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    suspend fun getCurrentLocation(): Result<DeviceLocation> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(IllegalStateException("Permiso de ubicación denegado"))
        }
        requestCurrentLocation().map { location ->
            DeviceLocation(location.latitude, location.longitude, location.accuracy)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): Result<Location> = suspendCancellableCoroutine { cont ->
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) cont.resume(Result.success(location))
                else cont.resume(Result.failure(IllegalStateException("No se pudo obtener la ubicación")))
            }
            .addOnFailureListener { exception ->
                if (cont.isActive) cont.resume(Result.failure(exception))
            }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }
}

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)
