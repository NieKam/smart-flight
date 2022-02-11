package kniezrec.com.flightinfo.startup

import android.app.Application
import kniezrec.com.flightinfo.BuildConfig
import kniezrec.com.flightinfo.db.CitiesDatabaseHelper
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class SmartFlightApplication : Application() {

  private val mDbHelper: CitiesDatabaseHelper by lazy {
    CitiesDatabaseHelper(this)
  }

  fun getDbHelper(): CitiesDatabaseHelper {
    return mDbHelper
  }

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}