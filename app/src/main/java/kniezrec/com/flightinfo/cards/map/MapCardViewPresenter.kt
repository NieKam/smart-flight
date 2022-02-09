package kniezrec.com.flightinfo.cards.map

import android.content.Intent
import kniezrec.com.flightinfo.avionic.Course
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.common.Constants
import kniezrec.com.flightinfo.common.toggle
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.services.LocationService
import kniezrec.com.flightinfo.services.SensorService
import kniezrec.com.flightinfo.services.location.LocationUpdateCallback
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import java.io.File

/**
 * Copyright by Kamil Niezrecki
 */
typealias ZoomListener = (() -> Unit)
typealias MapCopiedCallback = ((Boolean) -> Unit)

class MapCardViewPresenter(
    private val mPreferences: FlightAppPreferences,
    private val mDataSource: CitiesDataSource
) :
    ServiceBasedCardPresenter<MapCardViewPresenter.ViewContract>() {

  interface ViewContract : ServiceBasedCardPresenter.ViewContract {
    fun drawPointOnMap(lat: Double, long: Double)
    fun centerOnMap(lat: Double, long: Double)
    fun rotateLocationMarker(rotation: Float)
    fun reloadMap()
    fun centerOnDefaultPoint()
    fun clearPath()
    fun drawPath(pointA: GeoPoint, pointB: GeoPoint)
    fun changeCardSize(expand: Boolean)
    fun registerRouteChangedReceiver()
    fun unregisterRouteChangedReceiver()
    fun copyMap(callback: MapCopiedCallback)
    fun getCacheDir(): File
    fun registerOnMaxZoomReachedListener(listener: ZoomListener)
    fun showZoomTip()
  }

  val defaultGeoPoint = GeoPoint(32.0, -32.0)
  private val mLocationCallback : LocationUpdateCallback = { location ->
    view?.drawPointOnMap(location.latitude, location.longitude)
    if (!mCenteredForTheFirstDraw) {
      view?.centerOnMap(location.latitude, location.longitude)
      mCenteredForTheFirstDraw = true
    }
  }

  private var mIsBoundToLocationService = false
  private var mIsBoundToSensorService = false
  private var mCenteredForTheFirstDraw = false
  private var mIsCardExpanded = false
  private var mLocationService: LocationService? = null
  private var mSensorService: SensorService? = null

  override fun onViewAttached(viewContract: ViewContract) {
    super.onViewAttached(viewContract)
    viewContract.apply {
      connectToLocationService()
      connectToSensorService()
      registerRouteChangedReceiver()
      registerOnMaxZoomReachedListener { onMaxZoomReached() }
    }

    loadSavedPointsAsync()
    checkMapZipFile()
  }

  override fun onViewDetached(viewContract: ViewContract) {
    super.onViewDetached(viewContract)

    if (mIsBoundToLocationService) {
      mLocationService?.removeLocationCallbackClient(mLocationCallback)
      viewContract.disconnectFromLocationService()
    }

    if (mIsBoundToSensorService) {
      mSensorService?.removeCourseCallbackClient(mCourseCallbackLazy)
      viewContract.disconnectFromSensorService()
    }

    viewContract.unregisterRouteChangedReceiver()
  }

  private fun checkMapZipFile() {
    val file = File(requiredNotNullView.getCacheDir(), Constants.OSMDROID_FILE)
    if (file.exists()) {
      Timber.i("Map is on the right place")
      return
    }

    Timber.i("Need to copy map file from assets")
    requiredNotNullView.copyMap { res ->
      if (res) {
        Timber.i("Results OK, need to reload map")
        view?.reloadMap()
      } else {
        Timber.e("Error during map copying")
      }
    }
  }

  fun onGoToLocationCLicked() {
    val lastLocation = mLocationService?.getLastObtainedLocation()
    if (lastLocation == null) {
      requiredNotNullView.centerOnDefaultPoint()
    } else {
      val lat: Double = lastLocation.latitude
      val long: Double = lastLocation.longitude
      requiredNotNullView.centerOnMap(lat, long)
    }
  }

  fun onResizeButtonClicked() {
    mIsCardExpanded = mIsCardExpanded.toggle()
    requiredNotNullView.changeCardSize(mIsCardExpanded)
  }

  fun onRouteChanged(intent: Intent) {
    val viewContract = view ?: return

    if (intent.hasExtra(Constants.ROUTE_NOT_READY_KEY)) {
      viewContract.clearPath()
    } else {
      intent.let {
        val pointA = it.getParcelableExtra<GeoPoint>(Constants.GEOPOINT_A)
        val pointB = it.getParcelableExtra<GeoPoint>(Constants.GEOPOINT_B)
        viewContract.drawPath(requireNotNull(pointA), requireNotNull(pointB))
      }
    }
  }

  private fun loadSavedPointsAsync() = CoroutineScope(Dispatchers.Main).launch {
    val cityAJob =
        async(Dispatchers.IO) { mDataSource.getCityById(mPreferences.getSavedCityAId()) }
    val cityBJoB =
        async(Dispatchers.IO) { mDataSource.getCityById(mPreferences.getSavedCityBId()) }

    val cityA = cityAJob.await()
    val cityB = cityBJoB.await()
    if (cityA != null && cityB != null) {
      val geoPointA = GeoPoint(cityA.latitude, cityA.longitude)
      val geoPointB = GeoPoint(cityB.latitude, cityB.longitude)
      view?.drawPath(geoPointA, geoPointB)
    }
  }

  override fun onSensorServiceConnected(service: SensorService?) {
    Timber.i("Connected to SensorService")
    mIsBoundToSensorService = true
    mSensorService = service
    mSensorService?.addCourseCallbackClient(mCourseCallbackLazy)
  }

  override fun onSensorServiceDisconnected() {
    mIsBoundToSensorService = false
    mSensorService = null
    Timber.e("Disconnected from SensorService")
  }

  override fun onLocationServiceConnected(service: LocationService?) {
    Timber.i("Connected to LocationService")
    mIsBoundToLocationService = true
    mLocationService = service
    mLocationService?.addLocationCallbackClient(mLocationCallback)
  }

  override fun onLocationServiceDisconnected() {
    mIsBoundToLocationService = false
    mLocationService = null
    Timber.e("Disconnected from LocationService")
  }

  private fun onMaxZoomReached() {
    if (mPreferences.isBiggerZoomEnabled() || mPreferences.wasZoomTipShown()) {
      return
    }
    mPreferences.markZoomTipAsShown()
    view?.showZoomTip()
  }

  private val mCourseCallbackLazy by lazy {
    object : SensorService.CourseCallback {
      override fun onCourseFixed(course: Course) {
        view?.rotateLocationMarker(course.azimuthNormalized.toFloat())
      }
    }
  }
}