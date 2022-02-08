package kniezrec.com.flightinfo.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kniezrec.com.flightinfo.MainActivity
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.permission.PermissionManager
import kniezrec.com.flightinfo.common.Constants
import kniezrec.com.flightinfo.common.Constants.Companion.APP_LIFECYCLE_ACTION
import kniezrec.com.flightinfo.common.Constants.Companion.NOTIFICATION_DISMISSED_ACTION
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.common.NotificationBroadcastReceiver
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

private const val OPEN_APP_REQUEST_CODE: Int = 0
private const val STOP_GPS_REQUEST_CODE = 1
private const val NOTIFICATION_ID = 777
private const val MIN_TIME = 100L
private const val MIN_DISTANCE = 10F
private const val CHANNEL_ID = "Location Info"

class LocationService : Service(), LocationListener, GpsStatus.Listener {

  interface LocationCallback {
    fun onGpsStatusChanged(satellites: Iterable<GpsSatellite>?)
    fun onLocationChanged(location: Location)
  }

  private val mCallbackClients: HashSet<LocationCallback> = HashSet()
  private val mBinder: IBinder = LocalBinder()

  private var mNotificationManager: NotificationManager? = null
  private lateinit var mLocationManager: LocationManager
  private var mIsLocationEnabled = false
  private var mGpsStatus: GpsStatus? = null
  private var mLastObtainedLocation: Location? = null
  private lateinit var mPreferences: FlightAppPreferences

  private val mNotification: Notification by lazy {
    initNotification()
  }

  inner class LocalBinder : Binder() {
    fun getService(): LocationService {
      return this@LocationService
    }
  }

  override fun onCreate() {
    super.onCreate()
    mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val appLifecycleIntentFilter = IntentFilter(APP_LIFECYCLE_ACTION)
    LocalBroadcastManager.getInstance(this).registerReceiver(
        mAppStateBroadcastReceiver,
        appLifecycleIntentFilter)

    val permissionIntent = IntentFilter(Navigation.getActionForPermission(Manifest.permission.ACCESS_FINE_LOCATION))
    LocalBroadcastManager.getInstance(this).registerReceiver(mPermissionReceiver, permissionIntent)

    mPreferences = FlightAppPreferences(this)
  }

  override fun onDestroy() {
    stopLocationUpdates()
    LocalBroadcastManager.getInstance(this).apply {
      unregisterReceiver(mAppStateBroadcastReceiver)
      unregisterReceiver(mPermissionReceiver)
    }

    clearNotification()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder {
    Timber.i("LocationService: onBind")
    startLocationUpdates()
    return mBinder
  }

  fun requestStopGPS() {
    stopLocationUpdates()
  }

  @SuppressLint("MissingPermission")
  private fun startLocationUpdates() {
    if (mIsLocationEnabled) {
      Timber.i("Already started, ignoring...")
      return
    }

    if (!PermissionManager.hasLocationPermission(this)) {
      Timber.w("Missing location permission.")
      return
    }

    Timber.i("startLocationUpdates() called")
    mLocationManager.let {
      it.addGpsStatusListener(this)
      it.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
    }
    mIsLocationEnabled = true
  }

  private fun stopLocationUpdates() {
    if (!mIsLocationEnabled) {
      Timber.i("Already stopped, ignoring...")
      return
    }

    Timber.i("stopLocationUpdates() called")

    mLocationManager.let {
      it.removeGpsStatusListener(this)
      it.removeUpdates(this)
    }

    mIsLocationEnabled = false
  }

  /** GPS Status */
  @SuppressLint("MissingPermission")
  override fun onGpsStatusChanged(event: Int) {
    mGpsStatus = mLocationManager.getGpsStatus(mGpsStatus)
    val satellites = mGpsStatus?.satellites

    mCallbackClients.forEach {
      it.onGpsStatusChanged(satellites)
    }
  }

  fun isLocationObtained(): Boolean {
    return mLastObtainedLocation != null
  }

  fun getLastObtainedLocation(): Location? {
    return mLastObtainedLocation
  }

  /** LocationListener */

  override fun onLocationChanged(location: Location) {
    mLastObtainedLocation = location

    for (client in mCallbackClients) {
      client.onLocationChanged(location)
    }
  }

  override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

  /** End of LocationListener */

  fun addLocationCallbackClient(callback: LocationCallback) {
    mCallbackClients.add(callback)
    Timber.i("Added LocationCallback callback, total items %d", mCallbackClients.size)
  }

  fun removeLocationCallbackClient(callback: LocationCallback) {
    mCallbackClients.remove(callback)
    Timber.i("Removed LocationCallback callback, total items %d", mCallbackClients.size)
  }

  private fun showNotification() {
    mNotificationManager?.notify(NOTIFICATION_ID, mNotification)
  }

  private fun clearNotification() {
    mNotificationManager?.cancel(NOTIFICATION_ID)
  }

  private val mPermissionReceiver = object : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
      startLocationUpdates()
    }
  }

  private fun initNotification(): Notification {
    val deleteIntent = Intent(this, NotificationBroadcastReceiver::class.java)
    deleteIntent.action = NOTIFICATION_DISMISSED_ACTION
    val deleteContentIntent = PendingIntent.getBroadcast(
        this, STOP_GPS_REQUEST_CODE,
        deleteIntent, 0)

    val notificationIntent = Intent(this, MainActivity::class.java).apply {
      action = Intent.ACTION_MAIN
      addCategory(Intent.CATEGORY_LAUNCHER)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val contentIntent = PendingIntent.getActivity(
        this, OPEN_APP_REQUEST_CODE,
        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.channel_name)
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, name, importance)
      mNotificationManager?.createNotificationChannel(channel)
    }

    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.small_plane_icon)
        .setContentTitle(resources.getString(R.string.app_background_notification_title))
        .setContentText(resources.getString(R.string.app_background_notification_content))
        .setContentIntent(contentIntent)
        .setDeleteIntent(deleteContentIntent)
        .build()
  }

  private val mAppStateBroadcastReceiver = object : BroadcastReceiver() {

    private fun toggleLocationUpdates(isAppResumed: Boolean) {
      if (isAppResumed) {
        clearNotification()
        startLocationUpdates()
      } else {
        stopLocationUpdates()
      }
    }

    private fun canShowNotification(context: Context): Boolean {
      return !isLocationObtained() &&
          PermissionManager.hasLocationPermission(context) &&
          mPreferences.canShowNotification()
    }

    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action != APP_LIFECYCLE_ACTION) {
        return
      }

      val isAppForeground = requireNotNull(intent.extras).getBoolean(Constants.ON_APP_FOREGROUND_KEY)

      if (!canShowNotification(context)) {
        Timber.i("Can't show notification")
        toggleLocationUpdates(isAppForeground)
        return
      }

      Timber.i("Received Intent Action: %s, is app resumed %b", intent.action, isAppForeground)

      if (isAppForeground) {
        clearNotification()
        // Try to start location updates if it was stopped using swipe on notification.
        startLocationUpdates()
      } else {
        showNotification()
      }
    }
  }
}
