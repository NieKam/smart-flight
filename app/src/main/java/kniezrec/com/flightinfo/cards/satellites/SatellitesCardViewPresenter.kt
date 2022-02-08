package kniezrec.com.flightinfo.cards.satellites

import android.location.GpsSatellite
import android.location.Location
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.services.LocationService

/**
 * Copyright by Kamil Niezrecki
 */

class SatellitesCardViewPresenter :
    ServiceBasedCardPresenter<SatellitesCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun showOnBarChart(satellites: Iterable<GpsSatellite>?)
    }

    private var mService: LocationService? = null
    private var mIsBoundToLocationService = false

    override fun onViewAttached(viewContract: ViewContract) {
        super.onViewAttached(viewContract)
        viewContract.connectToLocationService()
    }

    override fun onViewDetached(viewContract: ViewContract) {
        super.onViewDetached(viewContract)
        if (mIsBoundToLocationService) {
            mService?.removeLocationCallbackClient(mLocationCallbackLazy)
            viewContract.disconnectFromLocationService()
        }
    }

    override fun onLocationServiceConnected(service: LocationService) {
        mIsBoundToLocationService = true
        mService = service
        service.addLocationCallbackClient(mLocationCallbackLazy)
    }

    override fun onLocationServiceDisconnected() {
        mIsBoundToLocationService = false
        mService = null
    }

    private val mLocationCallbackLazy by lazy {
        object : LocationService.LocationCallback {
            override fun onGpsStatusChanged(satellites: Iterable<GpsSatellite>?) {
                view?.showOnBarChart(satellites)
            }

            override fun onLocationChanged(location: Location) {}
        }
    }
}
