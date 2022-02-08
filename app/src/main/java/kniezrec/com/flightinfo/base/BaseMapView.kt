package kniezrec.com.flightinfo.base

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.MotionEvent
import kniezrec.com.flightinfo.common.Constants
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.MapView

/**
 * Copyright by Kamil Niezrecki
 */

private const val OVER_MAX_ZOOM = 9
private const val MAX_ZOOM = 6
private const val MIN_ZOOM = 1
private const val MAP_SOURCE = "MapquestOSM"
private const val MAP_EXTENSION = ".jpg"
private const val TILE_SIZE_PX = 256

class BaseMapView : MapView {
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context) : super(context)

  private val mPreferences: FlightAppPreferences = FlightAppPreferences(context)

  init {
    setupMapFiles()

    isFlingEnabled = true
    setMultiTouchControls(true)
    setUseDataConnection(false)
    invalidate()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mPreferences.registerPreferenceChangeListener(mChangeListener)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mPreferences.unregisterPreferenceChangeListener(mChangeListener)
  }

  private fun isBiggerZoomEnabled(): Boolean {
    return mPreferences.isBiggerZoomEnabled()
  }

  fun reloadMapFiles() {
    setupMapFiles()
    invalidate()
  }

  private fun setupMapFiles() {
    val file = Constants.getMapFile(context)
    val tileProvider: MapTileProviderBase = OfflineTileProvider(
        SimpleRegisterReceiver(context),
        arrayOf(file)
    )

    this.tileProvider = tileProvider
    setTileSource(
        FileBasedTileSource(
            MAP_SOURCE,
            MIN_ZOOM,
            if (isBiggerZoomEnabled()) OVER_MAX_ZOOM else MAX_ZOOM,
            TILE_SIZE_PX,
            MAP_EXTENSION,
            emptyArray<String>()
        )
    )
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    when (ev.action) {
      MotionEvent.ACTION_UP -> {
        parent.requestDisallowInterceptTouchEvent(false)
      }
      MotionEvent.ACTION_DOWN -> {
        parent.requestDisallowInterceptTouchEvent(true)
      }
    }
    return super.dispatchTouchEvent(ev)
  }

  private val mChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
    reloadMapFiles()
  }
}
