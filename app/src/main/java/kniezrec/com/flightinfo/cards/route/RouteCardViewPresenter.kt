package kniezrec.com.flightinfo.cards.route

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.avionic.calculators.DistanceCalculator
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.common.Constants
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.services.LocationService
import kniezrec.com.flightinfo.services.location.LocationUpdateCallback
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

private const val DASH = "-"

class RouteCardViewPresenter(
    private val mPreferences: FlightAppPreferences,
    private val mDataSource: CitiesDataSource
) :
    ServiceBasedCardPresenter<RouteCardViewPresenter.ViewContract>() {

  interface ViewContract : ServiceBasedCardPresenter.ViewContract {
    fun showCityA(city: String)
    fun showCityB(city: String)
    fun setDistanceBetweenCities(distance: String)
    fun setRemainingRouteDistance(distanceToB: String)
    fun setArrivalTime(arrivalTime: String)
    fun removeCityAName()
    fun removeCityBName()
    fun showRouteDetailsLabels()
    fun hideAllLabels()
    fun notifyRouteChanged(intent: Intent)
    fun pickCityA(@StringRes title: Int, city: City?)
    fun pickCityB(@StringRes title: Int, city: City?)
  }

  private val mChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (key == Constants.DISTANCE_UNIT_PREFERENCE_KEY) {
      updateDistanceBetweenAB()
    }
  }

  private val mLocationCallback : LocationUpdateCallback = {
    updateRemainingRouteInfo(it)
  }

  private var mIsBoundToLocationService = false
  private var mLocationService: LocationService? = null
  private var mCityA: City? = null
  private var mCityB: City? = null

  override fun onViewAttached(viewContract: ViewContract) {
    super.onViewAttached(viewContract)
    viewContract.connectToLocationService()
    mPreferences.registerPreferenceChangeListener(mChangeListener)
    loadDataAsync()
  }

  override fun onViewDetached(viewContract: ViewContract) {
    super.onViewDetached(viewContract)
    if (mIsBoundToLocationService) {
      mLocationService?.removeLocationCallbackClient(mLocationCallback)
      viewContract.disconnectFromLocationService()
    }

    mPreferences.unregisterPreferenceChangeListener(mChangeListener)
  }

  fun onButtonAClicked() {
    requiredNotNullView.pickCityA(
        R.string.pick_up_departure,
        mCityA
    )
  }

  fun onButtonBClicked() {
    requiredNotNullView.pickCityB(
      R.string.pick_up_destination,
      mCityB
    )
  }

  fun onCityAResultsReceived(activityResult: ActivityResult) {
    getCityFromResults(activityResult)?.let { city ->
      onCityASet(city, false)
    }
  }

  fun onCityBResultsReceived(activityResult: ActivityResult) {
    getCityFromResults(activityResult)?.let { city ->
      onCityBSet(city, false)
    }
  }

  private fun getCityFromResults(activityResult: ActivityResult) : City? {
    if (activityResult.resultCode != Activity.RESULT_OK) {
      return null
    }

    val data = requireNotNull(activityResult.data)
    return requireNotNull(data.getParcelableExtra(Constants.CITY_EXTRA_KEY)) as City
  }

  private fun onCityASet(city: City, readFromPrefs: Boolean) {
    mCityA = city

    if (mCityB != null) {
      updateDistanceBetweenAB()
    }

    if (!readFromPrefs) {
      mPreferences.saveCityA(city.id)
      view?.notifyRouteChanged(getRouteChangeIntent())
    }

    view?.showCityA(city.name)
  }

  private fun onCityBSet(city: City, readFromPrefs: Boolean) {
    mCityB = city
    setRouteDetailsData()

    if (!readFromPrefs) {
      mPreferences.saveCityB(city.id)
      view?.notifyRouteChanged(getRouteChangeIntent())
    }

    view?.showCityB(city.name)
  }

  private fun loadDataAsync() = CoroutineScope(Dispatchers.Main).launch {
    val cityAJob =
        async(Dispatchers.IO) { mDataSource.getCityById(mPreferences.getSavedCityAId()) }
    val cityBJob =
        async(Dispatchers.IO) { mDataSource.getCityById(mPreferences.getSavedCityBId()) }

    val cityA = cityAJob.await()
    val cityB = cityBJob.await()

    if (cityA != null) {
      Timber.i("City A loaded from prefs: ${cityA.name}")
      onCityASet(cityA, true)
    }

    if (cityB != null) {
      Timber.i("City B loaded from prefs: ${cityB.name}")
      onCityBSet(cityB, true)
    }
  }

  fun onDeleteButtonClicked() {
    mCityA = null
    mCityB = null

    mPreferences.apply {
      clearCityA()
      clearCityB()
    }

    requiredNotNullView.apply {
      removeCityAName()
      removeCityBName()
      hideAllLabels()
      notifyRouteChanged(getRouteChangeIntent())
    }
  }

  fun onLongCityAClicked(): Boolean {
    mCityA = null
    mPreferences.clearCityA()

    requiredNotNullView.apply {
      removeCityAName()
      setDistanceBetweenCities(DASH)
      notifyRouteChanged(getRouteChangeIntent())
    }

    return true
  }

  fun onLongCityBClicked(): Boolean {
    mCityB = null
    mPreferences.clearCityB()

    requiredNotNullView.apply {
      removeCityBName()
      hideAllLabels()
      notifyRouteChanged(getRouteChangeIntent())
    }

    return true
  }

  override fun onLocationServiceConnected(service: LocationService?) {
    Timber.i("Connected to LocationService")
    mIsBoundToLocationService = true
    mLocationService = service
    mLocationService?.addLocationCallbackClient(mLocationCallback)
  }

  override fun onLocationServiceDisconnected() {
    mIsBoundToLocationService = false
    mLocationService?.removeLocationCallbackClient(mLocationCallback)
    mLocationService = null
    Timber.e("Disconnected from LocationService")
  }

  private fun getRouteChangeIntent(): Intent {
    return Navigation.getIntentForRouteChange(mCityA, mCityB)
  }

  private fun setRouteDetailsData() {
    checkNotNull(mCityB) { "City B cannot be null" }
    updateDistanceBetweenAB()
    updateRemainingRouteInfo(mLocationService?.getLastObtainedLocation())

    view?.showRouteDetailsLabels()
  }

  private fun updateDistanceBetweenAB() {
    val localCityB = mCityB ?: return
    val localCityA = mCityA ?: return

    val unit = mPreferences.getDistanceUnitValue()
    val distanceBetween = DistanceCalculator.getDistance(unit, localCityA, localCityB)
    view?.setDistanceBetweenCities(distanceBetween)
  }

  private fun updateRemainingRouteInfo(location: Location?) {
    if (location == null || mCityB == null) {
      return
    }
    val cityB = mCityB as City
    val unit = mPreferences.getDistanceUnitValue()

    val distanceToB = DistanceCalculator.getDistance(unit, location, cityB)
    val remainingTime = DistanceCalculator.getEstimatedTimeToLocation(location, cityB)

    view?.apply {
      setRemainingRouteDistance(distanceToB)
      setArrivalTime(remainingTime)
    }
  }
}
