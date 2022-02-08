package kniezrec.com.flightinfo.cards.map

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.ResultReceiver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.ResizableActivity
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.common.setCustomMarker
import kniezrec.com.flightinfo.common.snackbar.TopSnackbar
import kniezrec.com.flightinfo.databinding.MapCardLayoutBinding
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/**
 * Copyright by Kamil Niezrecki
 */

private const val DEFAULT_ZOOM = 3
private const val PATH_WIDTH = 7f

class MapCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<MapCardViewPresenter>(context, attrs, defStyleAttr),
    MapCardViewPresenter.ViewContract {

  private val mRouteChangedReceiver = object : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent) {
      mPresenter.onRouteChanged(intent)
    }
  }

  private var mMapController: IMapController
  private var mLocationMarker: Marker
  private var mPolylinePath: Polyline
  private var mSnackBar: TopSnackbar? = null

  private val mBinding : MapCardLayoutBinding =
    MapCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    mBinding.apply {
      btnResize.setOnClickListener { mPresenter.onResizeButtonClicked() }
      btnGoToMyLocation.setOnClickListener { mPresenter.onGoToLocationCLicked() }
    }

    mMapController = mBinding.mapView.controller
    mLocationMarker = Marker(mBinding.mapView)
    mPolylinePath = Polyline()
    setupMap()
  }

  override fun initPresenter(): MapCardViewPresenter {
    return MapCardViewPresenter(FlightAppPreferences(context), CitiesDataSource(context))
  }

  private fun setupMap() {
    mPolylinePath.apply {
      color = ContextCompat.getColor(context, R.color.purple_dark)
      width = PATH_WIDTH
      isGeodesic = true
    }

    mLocationMarker.apply {
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
      setCustomMarker(context, R.drawable.small_plane_icon)
    }

    mMapController.apply {
      setCenter(mPresenter.defaultGeoPoint)
      setZoom(DEFAULT_ZOOM)
    }

    mBinding.mapView.invalidate()
  }

  override fun getCacheDir(): File {
    return context.cacheDir
  }

  override fun registerOnMaxZoomReachedListener(listener: ZoomListener) {
    mBinding.mapView.setMapListener(object : MapListener {
      override fun onScroll(event: ScrollEvent?) = true

      override fun onZoom(event: ZoomEvent): Boolean {
        if (event.source.maxZoomLevel == event.zoomLevel) {
          listener.invoke()
        }
        return true
      }
    })
  }

  override fun showZoomTip() {
    if (mSnackBar?.isShown == true) {
      return
    }

    mSnackBar =
        TopSnackbar.make(rootView.findViewById(R.id.toolbar), R.string.max_zoom_tip, TopSnackbar.LENGTH_VERY_LONG)
            .apply {
              setBackgroundColor(R.color.purple_dark)
              setAction(R.string.settings) {
                Navigation.goToSettings(context, highlightCustomSettings = true)
                Handler().postDelayed({ mSnackBar?.dismiss() }, 1000) // Hide in delay to avoid animation issues.
              }
            }.also {
              it.show()
            }
  }

  override fun onDetachedFromWindow() {
    mSnackBar?.dismiss()
    mLocationMarker.onDetach(mBinding.mapView)
    mPolylinePath.onDetach(mBinding.mapView)
    mBinding.mapView.overlays?.clear()
    Marker.cleanDefaults()
    super.onDetachedFromWindow()
  }

  override fun drawPath(pointA: GeoPoint, pointB: GeoPoint) {
    mPolylinePath.points = listOf(pointA, pointB)
    mBinding.mapView.overlays.add(mPolylinePath)
    mBinding.mapView.invalidate()
  }

  override fun clearPath() {
    mBinding.mapView.overlays.remove(mPolylinePath)
    mBinding.mapView.invalidate()
  }

  override fun rotateLocationMarker(rotation: Float) {
    mLocationMarker.rotation = rotation
    mBinding.mapView.invalidate()
  }

  override fun drawPointOnMap(lat: Double, long: Double) {
    val geoPoint = GeoPoint(lat, long)
    mLocationMarker.position = geoPoint
    mBinding.mapView.overlays?.add(0, mLocationMarker)
  }

  override fun centerOnMap(lat: Double, long: Double) {
    val geoPoint = GeoPoint(lat, long)
    mMapController.setCenter(geoPoint)
  }

  override fun reloadMap() {
    mBinding.mapView.reloadMapFiles()
  }

  override fun centerOnDefaultPoint() {
    mMapController.apply {
      setCenter(mPresenter.defaultGeoPoint)
      setZoom(DEFAULT_ZOOM)
    }
  }

  override fun needsLocationPermission(): Boolean {
    return true
  }

  override fun changeCardSize(expand: Boolean) {
    val valAnimator: ValueAnimator

    if (expand) {
      valAnimator = ValueAnimator.ofInt(height, (height * 2)).apply {
        addUpdateListener { animation ->
          changeHeight(animation.animatedValue as Int)
          scrollToBottom()
        }
      }
      animateIconChange(R.drawable.ic_shrink)
    } else {
      valAnimator = ValueAnimator.ofInt(height, (height / 2)).apply {
        addUpdateListener { animation ->
          changeHeight(animation.animatedValue as Int)
        }
      }
      animateIconChange(R.drawable.ic_expand)
    }

    valAnimator.apply {
      duration = context.resources.getInteger(R.integer.transition_duration).toLong()
      interpolator = AccelerateDecelerateInterpolator()
    }.start()
  }

  override fun registerRouteChangedReceiver() {
    val actionRouteChanged = IntentFilter(Navigation.getActionForRouteChange())
    LocalBroadcastManager.getInstance(context).registerReceiver(
        mRouteChangedReceiver,
        actionRouteChanged
    )
  }

  override fun unregisterRouteChangedReceiver() {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(mRouteChangedReceiver)
  }

  override fun copyMap(callback: MapCopiedCallback) {
    val mapHelper = MapHelper(context)
    CoroutineScope(Dispatchers.Main).launch {
      val res = async { mapHelper.doCopy() }
      callback.invoke(res.await())
    }
  }

  private fun changeHeight(height: Int) {
    mBinding.mvRootLayout.apply {
      layoutParams.height = height
    }.requestLayout()
  }

  private fun scrollToBottom() {
    (context as ResizableActivity).scrollToBottom()
  }

  private fun animateIconChange(@DrawableRes newIconResId: Int) {
    val duration = 180L
    val animOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_backwards)
    val animIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_forwards)

    animOut.duration = duration
    animIn.duration = duration

    animOut.setAnimationListener(object : AnimationListener {
      override fun onAnimationStart(animation: Animation) {}
      override fun onAnimationRepeat(animation: Animation) {}

      override fun onAnimationEnd(animation: Animation) {
        mBinding.btnResize.apply {
          setImageResource(newIconResId)
          startAnimation(animIn)
        }
      }
    })

    mBinding.btnResize.startAnimation(animOut)
  }
}