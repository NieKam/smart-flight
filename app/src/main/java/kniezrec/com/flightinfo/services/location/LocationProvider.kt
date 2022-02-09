package kniezrec.com.flightinfo.services.location

import android.location.Location

/**
 * Copyright by Kamil Niezrecki
 */

typealias LocationUpdateCallback = ((Location) -> Unit)
typealias SatellitesUpdateCallback = ((List<Satellite>) -> Unit)

interface LocationProvider {
    companion object {
        const val MIN_TIME = 100L
        const val MIN_DISTANCE = 10F
    }

    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun registerForLocationUpdates(callback: LocationUpdateCallback)
    fun registerForSatellitesUpdates(callback: SatellitesUpdateCallback)
    fun getLastObtainedLocation(): Location?
}