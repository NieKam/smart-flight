package kniezrec.com.flightinfo.cards.gps

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Messenger
import kniezrec.com.flightinfo.avionic.calculators.DistanceCalculator
import kniezrec.com.flightinfo.avionic.calculators.TimeCalculator
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.services.FindCityService
import kniezrec.com.flightinfo.services.LocationService
import kniezrec.com.flightinfo.services.location.LocationUpdateCallback
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

class LocationCardViewPresenter(private val mPreferences: FlightAppPreferences) :
    ServiceBasedCardPresenter<LocationCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun setCityText(city: String)
        fun setCountryText(country: String)
        fun setDistanceText(distance: String)
        fun setTimeText(time: String)
    }

    private val mHandlerCallback = Handler.Callback { msg ->
        if (msg.what == FindCityService.Contract.MSG_CITY_FOUND && msg.obj != null) {
            handleCityFound(msg.obj as City)
            true
        } else {
            false
        }
    }

    private val mIncomingMessenger = Messenger(Handler(Looper.getMainLooper(), mHandlerCallback))
    private val mTimezoneCalculator = TimeCalculator()
    private val mLocationCallback : LocationUpdateCallback = {
        sendFindCityMessage(it)
    }

    private var mIsBoundToFindCityService = false
    private var mIsBoundToLocationService = false
    private var mFindCityService: Messenger? = null
    private var mLocationService: LocationService? = null

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.apply {
            connectToLocationService()
            connectToCityFindService()
        }
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mIsBoundToFindCityService) {
            viewContract.disconnectFromCityFindService()
        }

        if (mIsBoundToLocationService) {
            mLocationService?.removeLocationCallbackClient(mLocationCallback)
            viewContract.disconnectFromLocationService()
        }
    }

    private fun handleCityFound(city: City) {
        Timber.i("Found city ${city.name}")
        view?.apply {
            setCityText(city.name)
            setCountryText(city.country)
            setTimeText(mTimezoneCalculator.getTimeInCity(city))
            setDistanceText(getDistance(city))
        }
    }

    fun sendFindCityMessage(location: Location) {
        val msg = Navigation.obtainFindCityMessage(location, mIncomingMessenger)
        mFindCityService?.send(msg)
    }

    override fun onLocationServiceConnected(service: LocationService?) {
        Timber.i("Connected to LocationService")
        mIsBoundToLocationService = true
        mLocationService = service?.also {
            it.addLocationCallbackClient(mLocationCallback)
        }
    }

    override fun onLocationServiceDisconnected() {
        mIsBoundToLocationService = false
        Timber.e("Disconnected from LocationService")
    }

    override fun onCityFinderServiceConnected(service: Messenger?) {
        Timber.i("Connected to FindCityService")
        mIsBoundToFindCityService = true
        mFindCityService = service
    }

    override fun onCityFinderServiceDisconnected() {
        mIsBoundToFindCityService = false
        Timber.e("Disconnected from FindCityService")
    }

    private fun getDistance(city: City): String {
        val lastLocation = mLocationService?.getLastObtainedLocation()
            ?: return DistanceCalculator.getZero(mPreferences.getDistanceUnitValue())

        return DistanceCalculator.getDistance(
            mPreferences.getDistanceUnitValue(),
            lastLocation,
            city
        )
    }
}