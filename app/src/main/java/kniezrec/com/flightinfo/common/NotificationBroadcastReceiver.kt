package kniezrec.com.flightinfo.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kniezrec.com.flightinfo.services.location.LocationService
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Constants.NOTIFICATION_DISMISSED_ACTION) {
      Timber.i("Dismissed notification receiver")
      stopGPSUpdates(context)
    }
  }

  private fun stopGPSUpdates(context: Context) {
    val intent = Navigation.getLocationServiceIntent(context)
    val binder: IBinder? = peekService(context, intent)
    if (binder != null) {
      (binder as LocationService.LocalBinder).getService().requestStopGPS()
    }
  }
}