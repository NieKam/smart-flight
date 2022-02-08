package kniezrec.com.flightinfo.settings

import android.os.Bundle
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.base.BaseActivity
import kniezrec.com.flightinfo.common.Navigation

private const val TAG = "SettingsFragment"

class SettingsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      title = getString(R.string.settings)
    }

    if (supportFragmentManager.findFragmentByTag(TAG) == null) {
      val fragment = SettingsFragment().apply {
        arguments = intent.extras
      }

      supportFragmentManager.beginTransaction()
          .add(R.id.settings_container, fragment, TAG)
          .commit()
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    Navigation.exitZoomInFadeOut(this)
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
