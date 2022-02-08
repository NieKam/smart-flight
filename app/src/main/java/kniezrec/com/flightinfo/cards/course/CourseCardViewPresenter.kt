package kniezrec.com.flightinfo.cards.course

import android.content.pm.PackageManager
import kniezrec.com.flightinfo.avionic.Course
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.common.Constants.Companion.DEGREE_CHAR
import kniezrec.com.flightinfo.services.SensorService
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

class CourseCardViewPresenter : ServiceBasedCardPresenter<CourseCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun setCourseText(label: String)
        fun setCourseLiteralText(label: String)
        fun hideCard()
        fun rotatePlane(azimuth: Float)
        fun showOverlay()
    }

    private var mServiceBounded = false
    private var mService: SensorService? = null
    private val mRequiredSensors = listOf(
        PackageManager.FEATURE_SENSOR_ACCELEROMETER,
        PackageManager.FEATURE_SENSOR_COMPASS
    )

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.let {
            it.connectToSensorService()
            if (!it.areFeaturesSupported(mRequiredSensors)) {
                it.showOverlay()
            }
        }
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mServiceBounded) {
            mService?.removeCourseCallbackClient(mSensorCallbackLazy)
            viewContract.disconnectFromSensorService()
        }
    }

    override fun onSensorServiceConnected(service: SensorService?) {
        Timber.i("Connected to SensorService")
        mServiceBounded = true
        mService = service?.also {
            it.addCourseCallbackClient(mSensorCallbackLazy)
        }
    }

    override fun onSensorServiceDisconnected() {
        mServiceBounded = false
        Timber.e("Cannot connect to SensorService")
    }

    private val mSensorCallbackLazy by lazy {
        object :
            SensorService.CourseCallback {
            override fun onCourseFixed(course: Course) {
                view?.apply {
                    setCourseText(course.azimuthNormalized.toString().plus(DEGREE_CHAR))
                    setCourseLiteralText(course.abbreviation)
                    rotatePlane(course.azimuth.toFloat())
                }
            }
        }
    }

    fun onOverlayButtonClicked() {
        requiredNotNullView.hideCard()
    }
}
