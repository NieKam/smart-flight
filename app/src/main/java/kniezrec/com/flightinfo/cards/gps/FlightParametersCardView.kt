package kniezrec.com.flightinfo.cards.gps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.databinding.CourseCardLayoutBinding
import kniezrec.com.flightinfo.databinding.FlightParametersCardLayoutBinding
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */

class FlightParametersCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<FlightParametersCardViewPresenter>(context, attrs, defStyleAttr),
    FlightParametersCardViewPresenter.ViewContract {

    private val mBinding : FlightParametersCardLayoutBinding =
        FlightParametersCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun initPresenter(): FlightParametersCardViewPresenter {
        return FlightParametersCardViewPresenter(FlightAppPreferences(context))
    }

    override fun setSpeedText(speed: String) {
        mBinding.speedValueText.text = speed
    }

    override fun setVerticalSpeedText(vs: String) {
        mBinding.vsSpeedValueText.text = vs
    }

    override fun setAltitudeText(altitude: String) {
        mBinding.altValueText.text = altitude
    }

    override fun setPressureText(pressure: String) {
        mBinding.pressureValueText.text = pressure
    }

    override fun needsLocationPermission(): Boolean {
        return true
    }
}