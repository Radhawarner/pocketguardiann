package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationState(
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val accuracyMeters: Float = 6.5f,
    val locationName: String = "Central Campus Quad, North Block",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true,
    val provider: String = "GPS",
    val permissionGranted: Boolean = false,
    val isDemoSimulation: Boolean = false
) {
    val mapsUrl: String
        get() = "https://maps.google.com/?q=$latitude,$longitude"

    val formattedTime: String
        get() = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(lastUpdated))

    val coordinatesDisplay: String
        get() = String.format(Locale.US, "%.5f° N, %.5f° W (±%.1fm)", latitude, Math.abs(longitude), accuracyMeters)
}

class LocationService(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationState = MutableStateFlow(
        LocationState(
            latitude = 37.7749,
            longitude = -122.4194,
            accuracyMeters = 5.0f,
            locationName = "University Main Quad",
            lastUpdated = System.currentTimeMillis(),
            isAvailable = true,
            permissionGranted = true,
            isDemoSimulation = true
        )
    )
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val demoLocations = listOf(
        Pair("KSRCT College Campus Gate", Pair(11.3658, 77.8285)),
        Pair("Downtown Metro Plaza, Platform 2", Pair(37.7891, -122.4014)),
        Pair("North Riverwalk Trailhead", Pair(37.7645, -122.4350)),
        Pair("Civic Center Library Walkway", Pair(37.7798, -122.4162)),
        Pair("Westside Transit Terminal", Pair(37.7523, -122.4187))
    )
    private var demoIndex = 0

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate(forceRealIfPermitted: Boolean = false) {
        val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val hasNet = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        if (locationManager != null && (hasGps || hasNet)) {
            try {
                val lastGps = if (hasGps) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNet = if (hasNet) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null
                val best = lastGps ?: lastNet

                if (best != null) {
                    _locationState.value = LocationState(
                        latitude = best.latitude,
                        longitude = best.longitude,
                        accuracyMeters = if (best.hasAccuracy()) best.accuracy else 10.0f,
                        locationName = "Current GPS Coordinates",
                        lastUpdated = System.currentTimeMillis(),
                        isAvailable = true,
                        provider = best.provider ?: "GPS",
                        permissionGranted = true,
                        isDemoSimulation = false
                    )
                    return
                }
            } catch (e: SecurityException) {
                // Permission not granted yet, fallback to simulation safely
            }
        }

        // Demo simulation cycle
        demoIndex = (demoIndex + 1) % demoLocations.size
        val (name, coords) = demoLocations[demoIndex]
        _locationState.value = LocationState(
            latitude = coords.first,
            longitude = coords.second,
            accuracyMeters = 4.2f + (demoIndex * 1.5f),
            locationName = name,
            lastUpdated = System.currentTimeMillis(),
            isAvailable = true,
            provider = "Demo GPS Fix",
            permissionGranted = true,
            isDemoSimulation = true
        )
    }

    fun setSimulatedLocation(name: String, lat: Double, lng: Double) {
        _locationState.value = _locationState.value.copy(
            latitude = lat,
            longitude = lng,
            locationName = name,
            lastUpdated = System.currentTimeMillis(),
            isDemoSimulation = true,
            isAvailable = true
        )
    }

    fun updatePermissionStatus(isGranted: Boolean) {
        _locationState.value = _locationState.value.copy(
            permissionGranted = isGranted
        )
        if (isGranted) {
            requestLocationUpdate()
        }
    }
}
