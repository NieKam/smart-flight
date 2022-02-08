package kniezrec.com.flightinfo.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.provider.Settings
import androidx.core.content.ContextCompat
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.services.FindCityService
import kniezrec.com.flightinfo.services.LocationService
import kniezrec.com.flightinfo.services.SensorService
import kniezrec.com.flightinfo.settings.SettingsActivity
import org.osmdroid.util.GeoPoint

/**
 * Copyright by Kamil Niezrecki
 */
object Navigation {

  @JvmStatic
  fun bindToSensorService(context: Context, connection: ServiceConnection) {
    val intent = Intent(context, SensorService::class.java)
    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
  }

  @JvmStatic
  fun bindToFindCityService(context: Context, connection: ServiceConnection) {
    val intent = Intent(context, FindCityService::class.java)
    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
  }

  @JvmStatic
  fun unbindFromService(context: Context, connection: ServiceConnection) {
    context.unbindService(connection)
  }

  fun getLocationServiceIntent(context: Context): Intent {
    return Intent(context, LocationService::class.java)
  }

  @JvmStatic
  fun bindToLocationService(context: Context, connection: ServiceConnection) {
    val intent = getLocationServiceIntent(context)
    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
  }

  fun getOnAppInBackgroundIntent(): Intent {
    val intent = Intent(Constants.APP_LIFECYCLE_ACTION)
    intent.putExtra(Constants.ON_APP_FOREGROUND_KEY, false)
    return intent
  }

  fun getOnAppInForegroundIntent(): Intent {
    val intent = Intent(Constants.APP_LIFECYCLE_ACTION)
    intent.putExtra(Constants.ON_APP_FOREGROUND_KEY, true)
    return intent
  }

  fun getActionForPermission(permission: String): String? {
    return permission + "_ACTION"
  }

  fun getPermissionReceiverIntent(permission: String): Intent {
    return Intent(getActionForPermission(permission)).apply {
      putExtra(Constants.PERMISSION, permission)
    }
  }

  fun getActionForRouteChange(): String {
    return Constants.ROUTE_CHANGE_ACTION
  }

  fun getIntentForRouteChange(mCityA: City?, mCityB: City?): Intent {
    val intent = Intent(getActionForRouteChange())

    if (mCityA != null && mCityB != null) {
      intent.putExtra(
          Constants.GEOPOINT_A,
          GeoPoint(mCityA.latitude, mCityA.longitude) as Parcelable
      )
      intent.putExtra(
          Constants.GEOPOINT_B,
          GeoPoint(mCityB.latitude, mCityB.longitude) as Parcelable
      )
    } else {
      intent.putExtra(Constants.ROUTE_NOT_READY_KEY, true)
    }

    return intent
  }

  fun enterZoomOutFadeIn(activity: Activity) {
    activity.overridePendingTransition(R.anim.zoom_out_fade_in, R.anim.zoom_out)
  }

  fun exitZoomInFadeOut(activity: Activity) {
    activity.overridePendingTransition(R.anim.zoom_in, R.anim.zoom_in_fade_out)
  }

  fun goToLocationSettings(context: Context) {
    ContextCompat.startActivity(
        context,
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), null
    )
  }

  fun goToSettings(context: Context, highlightCustomSettings: Boolean = false) {
    val intent = Intent(context, SettingsActivity::class.java).apply {
      putExtra(Constants.HIGHLIGHT_CUSTOM_SETTINGS, highlightCustomSettings)
    }
    ContextCompat.startActivity(
        context,
        intent,
        null
    )
    enterZoomOutFadeIn(context as Activity)
  }

  fun obtainFindCityMessage(location: Location, replyMessenger: Messenger): Message {
    return Message.obtain(null, FindCityService.Contract.MSG_FIND_CITY).apply {
      obj = location
      replyTo = replyMessenger
    }
  }
}
