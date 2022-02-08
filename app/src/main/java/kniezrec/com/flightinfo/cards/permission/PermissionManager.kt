package kniezrec.com.flightinfo.cards.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

/**
 * Copyright by Kamil Niezrecki
 */
object PermissionManager {

  private const val PERMISSION_REQUEST_ID = 101

  fun hasLocationPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
  }

  fun shouldShowSettingsPermission(activity: Activity): Boolean {
    return !ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        Manifest.permission.ACCESS_FINE_LOCATION)
  }

  fun openSettings(context: Context) {
    val uri = Uri.fromParts("package", context.packageName, null)
    Intent().let {
      it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
      it.data = uri
      context.startActivity(it)
    }
  }

  fun requestLocationPermission(context: Activity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_REQUEST_ID)
  }

  fun isGranted(permissions: Array<String>, grantResults: IntArray): Boolean {
    val resultsNotEmpty = permissions.isNotEmpty() && grantResults.isNotEmpty()
    return resultsNotEmpty && grantResults[0] == PackageManager.PERMISSION_GRANTED
  }
}