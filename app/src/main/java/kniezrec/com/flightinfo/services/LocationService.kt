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
import kniezrec.com.flightinfo.services.location.*
import kniezrec.com.flightinfo.services.location.LocationProvider
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

private const val OPEN_APP_REQUEST_CODE: Int = 0
private const val STOP_GPS_REQUEST_CODE = 1
private const val NOTIFICATION_ID = 777
private const val CHANNEL_ID = "Location Info"

class LocationService : Service() {
  private val mLocationCallbackClients: HashSet<LocationUpdateCallback> = HashSet()
  private val mGpsStatusCallbackClients: HashSet<SatellitesUpdateCallback> = HashSet()

  private val mBinder: IBinder = LocalBinder()

  private var mNotificationManager: NotificationManager? = null
  private var mIsLocationEnabled = false

  private lateinit var mPreferences: FlightAppPreferences
  private lateinit var mLocationProvider: LocationProvider

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
    mLocationProvider = getLocationProvider().also {
      it.registerForLocationUpdates { newLocation ->
        mLocationCallbackClients.forEach { client -> client.invoke(newLocation) }
      }

      it.registerForSatellitesUpdates { satellites ->
        mGpsStatusCallbackClients.forEach { client -> client.invoke(satellites) }
      }
    }

    mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val appLifecycleIntentFilter = IntentFilter(APP_LIFECYCLE_ACTION)
    LocalBroadcastManager.getInstance(this).registerReceiver(
      mAppStateBroadcastReceiver,
      appLifecycleIntentFilter
    )

    val permissionIntent =
      IntentFilter(Navigation.getActionForPermission(Manifest.permission.ACCESS_FINE_LOCATION))
    LocalBroadcastManager.getInstance(this)
      .registerReceiver(mPermissionReceiver, permissionIntent)

    mPreferences = FlightAppPreferences(this)
  }

  private fun getLocationProvider(): LocationProvider {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      LocationProviderApi24(manager)
    } else {
      LocationProviderImpl(manager)
    }
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
    mLocationProvider.startLocationUpdates()
    mIsLocationEnabled = true
  }

  private fun stopLocationUpdates() {
    if (!mIsLocationEnabled) {
      Timber.i("Already stopped, ignoring...")
      return
    }

    Timber.i("stopLocationUpdates() called")
    mLocationProvider.stopLocationUpdates()
    mIsLocationEnabled = false
  }

  fun getLastObtainedLocation(): Location? {
    return mLocationProvider.getLastObtainedLocation()
  }

  fun addGpsStatusChangeCallbackClient(callback: SatellitesUpdateCallback) {
    mGpsStatusCallbackClients.add(callback)
    Timber.i("Added SatellitesUpdateCallback callback, total items %d", mGpsStatusCallbackClients.size)
  }

  fun removeGpsStatusChangeCallbackClient(callback: SatellitesUpdateCallback) {
    mGpsStatusCallbackClients.remove(callback)
    Timber.i("Removed SatellitesUpdateCallback callback, total items %d", mGpsStatusCallbackClients.size)
  }

  fun addLocationCallbackClient(callback: LocationUpdateCallback) {
    mLocationCallbackClients.add(callback)
    Timber.i("Added LocationUpdateCallback callback, total items %d", mLocationCallbackClients.size)
  }

  fun removeLocationCallbackClient(callback: LocationUpdateCallback) {
    mLocationCallbackClients.remove(callback)
    Timber.i("Removed LocationUpdateCallback callback, total items %d", mLocationCallbackClients.size)
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
      val locationObtained = mLocationProvider.getLastObtainedLocation() != null
      return !locationObtained  && PermissionManager.hasLocationPermission(context) &&
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
