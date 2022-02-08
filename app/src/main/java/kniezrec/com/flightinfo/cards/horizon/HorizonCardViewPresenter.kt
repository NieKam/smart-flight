package kniezrec.com.flightinfo.cards.horizon

import android.content.pm.PackageManager
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.services.SensorService
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * Copyright by Kamil Niezrecki
 */

private const val SCALE = 4
private const val MAX_RANGE = 200F

class HorizonCardViewPresenter :
    ServiceBasedCardPresenter<HorizonCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun updateCoordinates(roll: Float, pitch: Float, yaw: Float)
        fun showOverlay()
        fun hide()
    }

    private val mRequiredSensor = listOf(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
    private var mIsBoundedToSensorService = false
    private var mService: SensorService? = null
    private var mPitchStartPosition: Float = 0F
    private var mPitchNeedsCalibrate = true

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.connectToSensorService()
        if (!viewContract.areFeaturesSupported(mRequiredSensor)) {
            viewContract.showOverlay()
        }
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mIsBoundedToSensorService) {
            viewContract.disconnectFromSensorService()
            mService?.removeRotationCallbackClient(mSensorCallbackLazy)
        }
    }

    override fun onSensorServiceConnected(service: SensorService?) {
        Timber.i("Connected to SensorService")
        mIsBoundedToSensorService = true
        mService = service?.also {
            it.addRotationCallbackClient(mSensorCallbackLazy)
        }
    }

    override fun onSensorServiceDisconnected() {
        mIsBoundedToSensorService = false
        mService = null
        Timber.e("Cannot connect to SensorService")
    }

    private fun ensureRange(value: Float, min: Float, max: Float): Float {
        return min(max(value, min), max)
    }

    private val mSensorCallbackLazy by lazy {
        object :
            SensorService.RotationCallback {
            override fun onRotation(roll: Float, pitch: Float, yaw: Float) {
                if (mPitchNeedsCalibrate) {
                    mPitchStartPosition = pitch
                    mPitchNeedsCalibrate = false
                }

                var scaledPitch = (mPitchStartPosition - pitch) * SCALE
                scaledPitch = ensureRange(scaledPitch, -MAX_RANGE, MAX_RANGE)
                view?.updateCoordinates(roll, scaledPitch, yaw)
            }
        }
    }

    fun onCalibrateClicked() {
        mPitchNeedsCalibrate = true
    }

    fun onOverlayButtonClicked() {
        requiredNotNullView.hide()
    }

    fun onCalibrateLongClicked(): Boolean {
        mPitchStartPosition = 0F
        return true
    }
}