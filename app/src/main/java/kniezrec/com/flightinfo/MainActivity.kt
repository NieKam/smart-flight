package kniezrec.com.flightinfo

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kniezrec.com.flightinfo.base.BaseActivity
import kniezrec.com.flightinfo.cards.base.ResizableActivity
import kniezrec.com.flightinfo.cards.permission.PermissionManager
import kniezrec.com.flightinfo.common.DialogUtils
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.common.empty
import kniezrec.com.flightinfo.databinding.ActivityMainBinding
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */

class MainActivity : BaseActivity(), ResizableActivity, MainActivityPresenter.ViewContract {

    private lateinit var mPresenter: MainActivityPresenter
    private lateinit var mBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mPresenter = MainActivityPresenter(FlightAppPreferences(this))
        mBinding.toolbar.let {
            it.title = String.empty()
            setSupportActionBar(it)
        }
    }

    override fun onStart() {
        super.onStart()
        mPresenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        mPresenter.detachView()
    }

    override fun onResume() {
        super.onResume()
        mBinding.mainContentLayout.cardContainer.refreshView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                mPresenter.onAboutClicked()
                true
            }

            R.id.settings -> {
                mPresenter.onSettingsClicked()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun addKeepScreenOnFlag() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun clearKeepScreenOnFlag() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun setOrientation(orientation: Int) {
        requestedOrientation = orientation
    }

    override fun showPermissionDeniedView() {
        mBinding.mainContentLayout.cardContainer.onPermissionDeniedPermanently()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionManager.isGranted(permissions, grantResults)) {
            mPresenter.onPermissionGranted(permissions)
            mBinding.mainContentLayout.cardContainer.onPermissionGranted()
        } else {
            mPresenter.onPermissionDenied()
        }
    }

    override fun isGPSEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun goToSettings() {
        Navigation.goToSettings(this)
    }

    override fun showAboutDialog() {
        DialogUtils.showAboutDialog(this)
    }

    override fun showGpsNotEnabledDialog() {
        DialogUtils.showGpsNotEnabledDialog(this)
    }

    override fun sendBroadcastToService(permission: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Navigation.getPermissionReceiverIntent(
                permission
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mBinding.mainContentLayout.cardContainer.onActivityResultsReceived(requestCode, resultCode, data)
    }

    override fun scrollToBottom() {
        mBinding.mainContentLayout.mainScrollView.post {
            mBinding.mainContentLayout.mainScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
}
