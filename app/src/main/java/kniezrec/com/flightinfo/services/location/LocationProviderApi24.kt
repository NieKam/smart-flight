package kniezrec.com.flightinfo.services.location

import android.annotation.SuppressLint
import android.location.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class LocationProviderApi24(private val locationManager: LocationManager) : LocationProvider {
    private val handler = Handler(Looper.getMainLooper())

    private var lastObtainedLocation: Location? = null
    private var satellitesUpdateCallback: SatellitesUpdateCallback? = null
    private var locationUpdateCallback: LocationUpdateCallback? = null

    private val locationListener = LocationListener { location ->
        lastObtainedLocation = location
        locationUpdateCallback?.invoke(location)
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val satellites = mutableListOf<Satellite>()
            val size = status.satelliteCount
            (0 until size).forEach { i->
                satellites.add(
                    Satellite(
                        usedInFix = status.usedInFix(i),
                        signalStrength = status.getCn0DbHz(i)
                    )
                )
            }
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