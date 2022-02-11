package kniezrec.com.flightinfo.cards.satellites

import kniezrec.com.flightinfo.cards.base.ServiceBasedCardPresenter
import kniezrec.com.flightinfo.services.location.LocationService
import kniezrec.com.flightinfo.services.location.Satellite
import kniezrec.com.flightinfo.services.location.SatellitesUpdateCallback

/**
 * Copyright by Kamil Niezrecki
 */

class SatellitesCardViewPresenter :
    ServiceBasedCardPresenter<SatellitesCardViewPresenter.ViewContract>() {

    interface ViewContract : ServiceBasedCardPresenter.ViewContract {
        fun showOnBarChart(satellites: List<Satellite>)
    }

    private val mLocationCallback : SatellitesUpdateCallback = {
        view?.showOnBarChart(it)
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
            mService?.removeGpsStatusChangeCallbackClient(mLocationCallback)
            viewContract.disconnectFromLocationService()
        }
    }

    override fun onLocationServiceConnected(service: LocationService) {
        mIsBoundToLocationService = true
        mService = service
        service.addGpsStatusChangeCallbackClient(mLocationCallback)
    }

    override fun onLocationServiceDisconnected() {
        mIsBoundToLocationService = false
        mService = null
    }
}
