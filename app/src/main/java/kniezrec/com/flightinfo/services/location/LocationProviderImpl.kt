package kniezrec.com.flightinfo.services.location

import android.annotation.SuppressLint
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kniezrec.com.flightinfo.services.location.LocationProvider.Companion.MIN_DISTANCE
import kniezrec.com.flightinfo.services.location.LocationProvider.Companion.MIN_TIME

class LocationProviderImpl(private val locationManager: LocationManager) : LocationProvider {
    private var lastObtainedLocation: Location? = null
    private var satellitesUpdateCallback: SatellitesUpdateCallback? = null
    private var locationUpdateCallback: LocationUpdateCallback? = null
    private var gpsStatus: GpsStatus? = null

    @SuppressLint("MissingPermission")
    private val gpsStatusListener = GpsStatus.Listener {
        gpsStatus = locationManager.getGpsStatus(gpsStatus)
        satellitesUpdateCallback?.let { callback ->
            val satellites = gpsStatus?.satellites?.map { gpsSatellite ->
                Satellite(usedInFix = gpsSatellite.usedInFix(), signalStrength = gpsSatellite.snr)
            }
            satellites?.let {
                callback.invoke(satellites)
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastObtainedLocation = location
            locationUpdateCallback?.invoke(location)
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        @Deprecated("")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        locationManager.let {
            it.addGpsStatusListener(gpsStatusListener)
            it.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                locationListener
            )
        }
    }

    override fun stopLocationUpdates() {
        locationManager.let {
            it.removeGpsStatusListener(gpsStatusListener)
            it.removeUpdates(locationListener)
        }
    }

    override fun registerForLocationUpdates(callback: LocationUpdateCallback) {
        locationUpdateCallback = callback
    }

    override fun registerForSatellitesUpdates(callback: SatellitesUpdateCallback) {
        satellitesUpdateCallback = callback
    }

    override fun getLastObtainedLocation(): Location? = lastObtainedLocation
}