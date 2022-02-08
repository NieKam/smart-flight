package kniezrec.com.flightinfo.cards.gps

import android.location.GpsSatellite
import android.location.Location
import kniezrec.com.flightinfo.avionic.Speed
import kniezrec.com.flightinfo.avionic.calculators.Altitude
import kniezrec.com.flightinfo.avionic.calculators.Pressure
import kniezrec.com.flightinfo.avionic.calculators.VerticalSpeedCalculator
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.services.LocationService
import kniezrec.com.flightinfo.services.SensorService
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

class FlightParametersCardViewPresenter(private val mPreferences: FlightAppPreferences) :
    ServiceBasedCardPresenter<FlightParametersCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun setSpeedText(speed: String)
        fun setVerticalSpeedText(vs: String)
        fun setAltitudeText(altitude: String)
        fun setPressureText(pressure: String)
    }

    private val mVSCalculator = VerticalSpeedCalculator()

    private var mIsBoundToLocationService = false
    private var mIsBoundToSensorService = false
    private var mLocationService: LocationService? = null
    private var mSensorService: SensorService? = null

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.apply {
            connectToSensorService()
            connectToLocationService()
        }
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mIsBoundToLocationService) {
            mLocationService?.removeLocationCallbackClient(mLocationCallbackLazy)
            viewContract.disconnectFromLocationService()
        }

        if (mIsBoundToSensorService) {
            mSensorService?.removePressureCallbackClient(mPressureCallbackLazy)
            viewContract.disconnectFromSensorService()
        }
    }

    override fun onLocationServiceConnected(service: LocationService?) {
        Timber.i("Connected to LocationService")
        mIsBoundToLocationService = true
        mLocationService = service?.also {
            it.addLocationCallbackClient(mLocationCallbackLazy)
        }
    }

    override fun onLocationServiceDisconnected() {
        mIsBoundToLocationService = false
        Timber.e("Disconnected from LocationService")
    }

    private fun handleLocationChange(location: Location) {
        val speed = Speed.getSpeed(mPreferences.getSpeedUnitValue(), location.speed)
        val alt = Altitude.getAltitude(mPreferences.getAltitudeUnitValue(), location.altitude)
        val vs = mVSCalculator.getVerticalSpeed(
            mPreferences.getVerticalSpeedUnitValue(),
            location.altitude,
            location.time
        )

        view?.apply {
            setSpeedText(speed)
            setAltitudeText(alt)
            setVerticalSpeedText(vs)
        }
    }

    override fun onSensorServiceConnected(service: SensorService?) {
        Timber.i("Connected to SensorService")
        mIsBoundToSensorService = true
        mSensorService = service?.also {
            it.addPressureCallbackClient(mPressureCallbackLazy)
        }
    }

    override fun onSensorServiceDisconnected() {
        mIsBoundToSensorService = false
        Timber.e("Disconnected from SensorService")
    }

    private val mPressureCallbackLazy by lazy {
        object : SensorService.PressureCallback {
            override fun onPressure(pressure: Float) {
                view?.setPressureText(
                    Pressure.getPressure(
                        mPreferences.getPressureUnitValue(),
                        pressure
                    )
                )
            }
        }
    }

    private val mLocationCallbackLazy by lazy {
        object : LocationService.LocationCallback {
            override fun onGpsStatusChanged(satellites: Iterable<GpsSatellite>?) {}

            override fun onLocationChanged(location: Location) {
                handleLocationChange(location)
            }
        }
    }

}