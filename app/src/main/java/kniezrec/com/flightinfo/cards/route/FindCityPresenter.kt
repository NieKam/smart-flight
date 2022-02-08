package kniezrec.com.flightinfo.cards.route

import android.content.pm.ActivityInfo
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Messenger
import kniezrec.com.flightinfo.base.BaseViewPresenter
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.services.FindCityService
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class FindCityPresenter(private val cityDataSource: CitiesDataSource, private val mPreferences: FlightAppPreferences) :
    BaseViewPresenter<FindCityPresenter.ViewContract>() {

  interface ViewContract {
    fun sendResults(city: City)
    fun displayCity(city: City)
    fun displayFoundCities(cities: List<City>)
    fun setOrientation(orientation: Int)
    fun showNoCityFoundDialog()
    fun connectToCityFinder()
    fun disconnectFromCityFinder()
    fun setConfirmButtonEnabled(isEnabled: Boolean)
    fun setSearchButtonEnabled(isEnabled: Boolean)
    fun displayDisambiguationInfo()
    fun hideDisambiguation()
  }

  private val mHandlerCallback = Handler.Callback { msg ->
    if (msg.what == FindCityService.Contract.MSG_CITY_FOUND && msg.obj != null) {
      onCityFound(msg.obj as City)
      true
    } else {
      false
    }
  }

  private val mIncomingMessenger = Messenger(Handler(Looper.getMainLooper(), mHandlerCallback))

  private var mFindCityService: Messenger? = null
  private var mCity: City? = null
  private var mIsBoundToFindCityService = false

  override fun onViewAttached() {
    super.onViewAttached()
    requiredNotNullView.connectToCityFinder()
    checkOrientationPreferences()
  }

  override fun onViewDetached() {
    if (mIsBoundToFindCityService) {
      requiredNotNullView.disconnectFromCityFinder()
    }
    super.onViewDetached()
  }

  private fun checkOrientationPreferences() {
    if (mPreferences.isPortraitModeForced()) {
      requiredNotNullView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    } else {
      requiredNotNullView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
    }
  }

  fun onCityFinderConnected(service: Messenger) {
    Timber.i("Connected to FindCityService")
    mIsBoundToFindCityService = true
    mFindCityService = service
  }

  fun onCityFinderDisconnected() {
    Timber.e("Disconnected from FindCityService")
    mIsBoundToFindCityService = false
    mFindCityService = null
  }

  fun onCityTextChanged(s: CharSequence) {
    val isNotEmpty = s.trim().isNotEmpty()
    requiredNotNullView.setSearchButtonEnabled(isNotEmpty)
  }

  fun onCityFound(city: City) {
    mCity = city
    view?.let {
      it.displayCity(city)
      it.setConfirmButtonEnabled(true)
      it.hideDisambiguation()
    }
  }

  fun onSearchButtonClicked(name: String) = CoroutineScope(Dispatchers.Main).launch {
    if (name.isEmpty()) {
      return@launch
    }

    val results = withContext(Dispatchers.IO) {
      searchCitiesBlocking(name)
    }

    handleCitySearchResults(results)
  }

  fun onConfirmClicked() {
    view?.sendResults(requireNotNull(mCity))
  }

  fun onLongPress(point: GeoPoint?) {
    if (point == null) {
      return
    }

    val location = Location("Map").apply {
      latitude = point.latitude
      longitude = point.longitude
    }

    findCity(location)
  }

  private fun findCity(location: Location) {
    val message = Navigation.obtainFindCityMessage(location, mIncomingMessenger)
    mFindCityService?.send(message)
  }

  fun onCityClicked(city: City) {
    mCity = city
    view?.let {
      it.displayCity(city)
      it.setConfirmButtonEnabled(true)
    }
  }

  private fun handleCitySearchResults(cities: ArrayList<City>) {
    val viewContract = view ?: return

    when (cities.size) {
      0 -> viewContract.apply {
        showNoCityFoundDialog()
        hideDisambiguation()
      }

      1 -> onCityFound(cities[0])

      else -> viewContract.apply {
        mCity = null
        displayFoundCities(cities)
        displayDisambiguationInfo()
        setConfirmButtonEnabled(false)
      }
    }
  }

  private fun searchCitiesBlocking(name: String): ArrayList<City> {
    return cityDataSource.getCitiesThatNameContain(name)
  }
}