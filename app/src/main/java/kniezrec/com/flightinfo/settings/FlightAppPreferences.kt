package kniezrec.com.flightinfo.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import kniezrec.com.flightinfo.common.Constants
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

private const val PREF_NAME = "LocalPrefs"
/* KEYS */
private const val CITY_A_KEY = "city_a_key"
private const val CITY_B_KEY = "city_b_key"
private const val IS_COURSE_CARD_HIDDEN = "is_course_card_hidden"
private const val IS_HORIZON_CARD_HIDDEN = "is_horizon_card_hidden"
private const val TIP_SHOWN_COUNT = "tip_shown_count"

private const val ZOOM_TIP_LIMIT = 4

class FlightAppPreferences(context: Context) {
    private val mPrefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    private val mDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun canShowNotification(): Boolean {
        return mDefaultPreferences.getBoolean(Constants.NOTIFICATION_PREFERENCE_KEY, true)
    }

    fun isScreenAwakeOn(): Boolean {
        return mDefaultPreferences.getBoolean(Constants.KEEP_SCREEN_PREFERENCE_KEY, false)
    }

    fun isBiggerZoomEnabled(): Boolean {
        return mDefaultPreferences.getBoolean(Constants.FORCE_ZOOM_PREFERENCE_KEY, false)
    }

    fun wasZoomTipShown(): Boolean {
        return mDefaultPreferences.getInt(TIP_SHOWN_COUNT, 0) >= ZOOM_TIP_LIMIT
    }

    fun markZoomTipAsShown() {
        val tipCount = mDefaultPreferences.getInt(TIP_SHOWN_COUNT, 0)
        mDefaultPreferences.edit().putInt(TIP_SHOWN_COUNT, tipCount + 1).apply()
    }

    fun isPortraitModeForced(): Boolean {
        return mDefaultPreferences.getBoolean(Constants.FORCE_PORTRAIT_MODE_PREFERENCE_KEY, true)
    }

    fun getSpeedUnitValue(): String {
        return mDefaultPreferences.getString(
            Constants.SPEED_UNIT_PREFERENCE_KEY,
            Constants.DEFAULT_VALUE
        ) as String
    }

    fun getVerticalSpeedUnitValue(): String {
        return mDefaultPreferences.getString(
            Constants.VERTICAL_SPEED_UNIT_PREFERENCE_KEY,
            Constants.DEFAULT_VALUE
        ) as String
    }

    fun getAltitudeUnitValue(): String {
        return mDefaultPreferences.getString(
            Constants.ALTITUDE_UNIT_PREFERENCE_KEY,
            Constants.DEFAULT_VALUE
        ) as String
    }

    fun getPressureUnitValue(): String {
        return mDefaultPreferences.getString(
            Constants.PRESSURE_UNIT_PREFERENCE_KEY,
            Constants.DEFAULT_VALUE
        ) as String
    }

    fun getDistanceUnitValue(): String {
        return mDefaultPreferences.getString(
            Constants.DISTANCE_UNIT_PREFERENCE_KEY,
            Constants.DEFAULT_VALUE
        ) as String
    }

    fun getSavedCityAId(): Int {
        return mPrefs.getInt(CITY_A_KEY, Constants.ERROR)
    }

    fun getSavedCityBId(): Int {
        return mPrefs.getInt(CITY_B_KEY, Constants.ERROR)
    }

    fun saveCityA(id: Int) {
        Timber.i("Save city A %d", id)
        mPrefs.edit { putInt(CITY_A_KEY, id) }
    }

    fun clearCityA() {
        mPrefs.edit { remove(CITY_A_KEY) }
    }

    fun clearCityB() {
        mPrefs.edit { remove(CITY_B_KEY) }
    }

    fun saveCityB(id: Int) {
        Timber.i("Save city B %d", id)
        mPrefs.edit { putInt(CITY_B_KEY, id) }
    }

    fun hideCourseCard() {
        mPrefs.edit { putBoolean(IS_COURSE_CARD_HIDDEN, true) }
    }

    fun isCourseCardHidden(): Boolean {
        return mPrefs.getBoolean(IS_COURSE_CARD_HIDDEN, false)
    }

    fun registerPreferenceChangeListener(changeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        mDefaultPreferences.registerOnSharedPreferenceChangeListener(changeListener)
    }

    fun unregisterPreferenceChangeListener(changeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        mDefaultPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)
    }

    fun hideHorizonCard() {
        mPrefs.edit { putBoolean(IS_HORIZON_CARD_HIDDEN, true) }
    }

    fun isHorizonCardHidden(): Boolean {
        return mPrefs.getBoolean(IS_HORIZON_CARD_HIDDEN, false)
    }
}