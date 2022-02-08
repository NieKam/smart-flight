package kniezrec.com.flightinfo

import android.content.pm.ActivityInfo
import kniezrec.com.flightinfo.base.BaseViewPresenter
import kniezrec.com.flightinfo.settings.FlightAppPreferences

class MainActivityPresenter(private val mPreferences: FlightAppPreferences) :
    BaseViewPresenter<MainActivityPresenter.ViewContract>() {

  interface ViewContract {
    fun addKeepScreenOnFlag()
    fun clearKeepScreenOnFlag()
    fun setOrientation(orientation: Int)
    fun showPermissionDeniedView()
    fun isGPSEnabled(): Boolean
    fun goToSettings()
    fun showAboutDialog()
    fun showGpsNotEnabledDialog()
    fun sendBroadcastToService(permission: String)
  }

  override fun onViewAttached() {
    super.onViewAttached()
    requiredNotNullView.let {
      if (!it.isGPSEnabled()) {
        it.showGpsNotEnabledDialog()
      }
    }

    checkScreenPreferences()
    checkOrientationPreferences()
  }

  private fun checkOrientationPreferences() {
    if (mPreferences.isPortraitModeForced()) {
      requiredNotNullView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    } else {
      requiredNotNullView.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
    }
  }

  private fun checkScreenPreferences() {
    if (mPreferences.isScreenAwakeOn()) {
      requiredNotNullView.addKeepScreenOnFlag()
    } else {
      requiredNotNullView.clearKeepScreenOnFlag()
    }
  }

  fun onAboutClicked() {
    requiredNotNullView.showAboutDialog()
  }

  fun onSettingsClicked() {
    requiredNotNullView.goToSettings()
  }

  fun onPermissionGranted(permissions: Array<String>) {
    view?.sendBroadcastToService(permissions[0])
  }

  fun onPermissionDenied() {
    view?.showPermissionDeniedView()
  }
}