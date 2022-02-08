package kniezrec.com.flightinfo.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

class AppVisibilityManager(val context: Context) {

  private val mActivityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  private val mPackageName = context.packageName
  private var mIsAppComesFromBackground = false

  fun onStart() {
    Timber.i("OnStart: Is app comes from background? %b", mIsAppComesFromBackground)
    if (mIsAppComesFromBackground) {
      LocalBroadcastManager.getInstance(context).sendBroadcast(Navigation.getOnAppInForegroundIntent())
    }
  }

  fun onStop() {
    if (!isApplicationVisible() && !isApplicationFinishing()) {
      LocalBroadcastManager.getInstance(context).sendBroadcast(Navigation.getOnAppInBackgroundIntent())
      mIsAppComesFromBackground = true
      Timber.i("OnStop: App goes background")
    }
  }

  private fun isApplicationFinishing(): Boolean {
    val activity = context.findContextOfType(Activity::class.java)
    return activity?.isFinishing ?: true
  }

  private fun isApplicationVisible(): Boolean {
    val runningProcesses = mActivityManager.runningAppProcesses
    return runningProcesses.any {
      it.processName == mPackageName &&
          (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
              || it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
    }
  }
}
