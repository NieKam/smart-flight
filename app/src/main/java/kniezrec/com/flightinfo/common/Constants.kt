package kniezrec.com.flightinfo.common

import android.content.Context
import java.io.File

/**
 * Copyright by Kamil Niezrecki
 */

class Constants {

  companion object {

      /** Results codes */
    const val ERROR = -1

    const val APP_LIFECYCLE_ACTION = "app_lifecycle_action"
    const val NOTIFICATION_DISMISSED_ACTION = "notification_dismissed"
    const val PICK_CITY_ACTION = "pick_city_action"
    const val ROUTE_CHANGE_ACTION = "route_notify_action"

    /** File paths */
    const val OSMDROID_FILE = "osmdroid.zip"

    /** Extra Keys */
    const val ON_APP_FOREGROUND_KEY = "on_foreground"
    const val CITY_EXTRA_KEY = "city_extra_key"
    const val TITLE_EXTRA_KEY = "title_extra_key"
    const val PERMISSION = "permission"
    const val GEOPOINT_A = "geopoint_a"
    const val GEOPOINT_B = "geopoint_b"
    const val ROUTE_NOT_READY_KEY = "route_not_ready"
    const val HIGHLIGHT_CUSTOM_SETTINGS = "highlight_custom_settings"

    /** Others */
    const val DEGREE_CHAR = 0x00B0.toChar()

    /** Settings */
    const val DEFAULT_VALUE = "1"
    const val SPEED_UNIT_PREFERENCE_KEY = "SPEED_UNIT_PREFERENCE_KEY"
    const val PRESSURE_UNIT_PREFERENCE_KEY = "PRESSURE_UNIT_PREFERENCE_KEY"
    const val ALTITUDE_UNIT_PREFERENCE_KEY = "ALTITUDE_UNIT_PREFERENCE_KEY"
    const val DISTANCE_UNIT_PREFERENCE_KEY = "DISTANCE_UNIT_PREFERENCE_KEY"
    const val NOTIFICATION_PREFERENCE_KEY = "NOTIFICATION_PREFERENCE_KEY"
    const val VERTICAL_SPEED_UNIT_PREFERENCE_KEY = "VERTICAL_SPEED_UNIT_PREFERENCE_KEY"
    const val KEEP_SCREEN_PREFERENCE_KEY = "KEEP_SCREEN_PREFERENCE_KEY"
    const val FORCE_ZOOM_PREFERENCE_KEY = "FORCE_ZOOM_PREFERENCE_KEY"
    const val FORCE_PORTRAIT_MODE_PREFERENCE_KEY = "FORCE_PORTRAIT_MODE_PREFERENCE_KEY"

    val UNIT_PREFS_ARRAY = arrayOf(
        SPEED_UNIT_PREFERENCE_KEY,
        PRESSURE_UNIT_PREFERENCE_KEY,
        ALTITUDE_UNIT_PREFERENCE_KEY,
        DISTANCE_UNIT_PREFERENCE_KEY,
        VERTICAL_SPEED_UNIT_PREFERENCE_KEY)

    /** Methods */
    fun getMapFile(context: Context): File = File(context.cacheDir, OSMDROID_FILE)
  }
}
