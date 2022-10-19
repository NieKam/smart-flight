package kniezrec.com.flightinfo.services.location

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.N)
class LocationProviderApi24(private val locationManager: LocationManager) : LocationProvider {
    private val handler = Handler(Looper.getMainLooper())

    private var lastObtainedLocation: Location? = null
    private var satellitesUpdateCallback: SatellitesUpdateCallback? = null
    private var locationUpdateCallback: LocationUpdateCallback? = null


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

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            Timber.d("onSatelliteStatusChanged ${status.satelliteCount}")
            val satellites = mutableListOf<Satellite>()
            val size = status.satelliteCount
            (0 until size).forEach { i ->
                satellites.add(
                    Satellite(
                        usedInFix = status.usedInFix(i),
                        signalStrength = status.getCn0DbHz(i)
                    )
                )
            }
            satellitesUpdateCallback?.invoke(satellites)
        }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        locationManager.let {
            it.registerGnssStatusCallback(gnssStatusCallback, handler)
            it.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LocationProvider.MIN_TIME,
                LocationProvider.MIN_DISTANCE,
                locationListener
            )
        }
    }

    override fun stopLocationUpdates() {
        locationManager.let {
            it.unregisterGnssStatusCallback(gnssStatusCallback)
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