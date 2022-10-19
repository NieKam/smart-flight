package kniezrec.com.flightinfo.cards.course

import android.content.pm.PackageManager
import android.location.Location
import kniezrec.com.flightinfo.avionic.Course
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.common.Constants.Companion.DEGREE_CHAR
import kniezrec.com.flightinfo.services.SensorService
import kniezrec.com.flightinfo.services.location.LocationService
import kniezrec.com.flightinfo.services.location.LocationUpdateCallback
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
        fun setBearingFromGps(bearing: Float)
    }

    private var mIsBoundToLocationService = false
    private var mIsBoundToSensorService = false
    private var mLocationService: LocationService? = null
    private var mSensorService: SensorService? = null

    private val mRequiredSensors = listOf(
        PackageManager.FEATURE_SENSOR_ACCELEROMETER,
        PackageManager.FEATURE_SENSOR_COMPASS
    )

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.let {
            it.connectToSensorService()
            it.connectToLocationService()
            if (!it.areFeaturesSupported(mRequiredSensors)) {
                it.showOverlay()
            }
        }
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mIsBoundToSensorService) {
            mSensorService?.removeCourseCallbackClient(mSensorCallbackLazy)
            viewContract.disconnectFromSensorService()
        }
        if (mIsBoundToLocationService) {
            mLocationService?.removeLocationCallbackClient(mLocationCallback)
            viewContract.disconnectFromLocationService()
        }
    }

    override fun onSensorServiceConnected(service: SensorService?) {
        Timber.i("Connected to SensorService")
        mIsBoundToSensorService = true
        mSensorService = service?.also {
            it.addCourseCallbackClient(mSensorCallbackLazy)
        }
    }

    override fun onSensorServiceDisconnected() {
        mIsBoundToSensorService = false
        Timber.e("Cannot connect to SensorService")
    }

    override fun onLocationServiceConnected(service: LocationService?) {
        Timber.i("Connected to LocationService")
        mIsBoundToLocationService = true
        mLocationService = service?.also {
            it.addLocationCallbackClient(mLocationCallback)
        }
    }

    override fun onLocationServiceDisconnected() {
        mIsBoundToLocationService = false
        Timber.e("Disconnected from LocationService")
    }

    private val mLocationCallback by lazy {
        object :
            LocationUpdateCallback {
            override fun invoke(location: Location) {
               view?.let { v ->
                   if (location.hasBearing()) {
                       v.setBearingFromGps(location.bearing)
                   }
               }
            }
        }
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
