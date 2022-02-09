package kniezrec.com.flightinfo.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.common.Constants

/**
 * Copyright by Kamil Niezrecki
 */
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

  companion object {
    const val PREF_NAME = "LocalPrefs"
  }

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    preferenceManager.sharedPreferencesName = PREF_NAME

    addPreferencesFromResource(R.xml.app_preferences_layout)
    if (arguments?.getBoolean(Constants.HIGHLIGHT_CUSTOM_SETTINGS) == true) {
      findPreference<CustomCheckBoxPreference>(Constants.FORCE_ZOOM_PREFERENCE_KEY)?.apply {
        animateBackground = true
      }
    }
  }

  override fun onResume() {
    super.onResume()
    registerChangeListener()
  }

  override fun onPause() {
    super.onPause()
    unregisterChangeListener()
  }

  override fun onStart() {
    super.onStart()
    initPreferences()
  }

  private fun initPreferences() {
    Constants.UNIT_PREFS_ARRAY.forEach {
      updatePreference(findPreference(it))
    }
  }

  private fun updatePreference(preference: Preference?) {
    if (preference != null && preference is ListPreference) {
      preference.setSummary(preference.entry)
    }
  }

  private fun registerChangeListener() {
    preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
  }

  private fun unregisterChangeListener() {
    preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
    updatePreference(findPreference(key))
  }
}