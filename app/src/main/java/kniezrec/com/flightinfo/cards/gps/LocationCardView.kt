package kniezrec.com.flightinfo.cards.gps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.databinding.LocationCardLayoutBinding
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */

class LocationCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<LocationCardViewPresenter>(context, attrs, defStyleAttr),
    LocationCardViewPresenter.ViewContract {

    private val mBinding : LocationCardLayoutBinding =
        LocationCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun initPresenter(): LocationCardViewPresenter {
        return LocationCardViewPresenter(FlightAppPreferences(context))
    }

    override fun setCityText(city: String) {
        mBinding.cityValueText.text = city
    }

    override fun setCountryText(country: String) {
        mBinding.countryValueText.text = country
    }

    override fun setDistanceText(distance: String) {
        mBinding.distanceValueText.text = distance
    }

    override fun setTimeText(time: String) {
        mBinding.timeValueText.text = time
    }

    override fun needsLocationPermission(): Boolean {
        return true
    }
}