package kniezrec.com.flightinfo.cards.route

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.common.*
import kniezrec.com.flightinfo.databinding.RouteCardLayoutBinding
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

private val LABELS_IDS = intArrayOf(
    R.id.distance_between_cities_label,
    R.id.distance_between_cities_value,
    R.id.distance_to_b_label,
    R.id.distance_to_b_value,
    R.id.time_to_b_label,
    R.id.time_to_b_value,
    R.id.delete_route_btn
)

private const val DURATION_MS = 200L
private const val CITY_LABEL_TOP_BIAS = 0.05f
private const val CITY_LABEL_DEF_BIAS = 0.5f
private const val PLANE_ICON_TOP_BIAS = 0.0f
private const val PLANE_ICON_DEF_BIAS = 0.8f

class RouteCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<RouteCardViewPresenter>(context, attrs, defStyleAttr),
    RouteCardViewPresenter.ViewContract {

    private val mConstraintSet = ConstraintSet()
    private val mBinding: RouteCardLayoutBinding =
        RouteCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val mPickCityA: ActivityResultLauncher<Intent>
    private val mPickCityB: ActivityResultLauncher<Intent>

    init {
        mBinding.chooseCityA.setOnClickListener { mPresenter.onButtonAClicked() }
        mBinding.chooseCityB.setOnClickListener { mPresenter.onButtonBClicked() }
        mBinding.cityA.apply {
            setOnLongClickListener { mPresenter.onLongCityAClicked() }
            setOnClickListener { mPresenter.onButtonAClicked() }
        }

        mBinding.cityB.apply {
            setOnLongClickListener { mPresenter.onLongCityBClicked() }
            setOnClickListener { mPresenter.onButtonBClicked() }
        }

        mBinding.deleteRouteBtn.setOnClickListener { mPresenter.onDeleteButtonClicked() }
        mPickCityA = (context as AppCompatActivity)
            .registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
                mPresenter.onCityAResultsReceived(res)
            }
        mPickCityB = context
            .registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
                mPresenter.onCityBResultsReceived(res)
            }
    }

    override fun initPresenter(): RouteCardViewPresenter {
        return RouteCardViewPresenter(FlightAppPreferences(context), CitiesDataSource(context))
    }

    override fun showCityA(city: String) {
        mBinding.cityA.apply {
            text = city
            visibility = View.VISIBLE
        }

        mBinding.chooseCityA.visibility = View.INVISIBLE
    }

    override fun showCityB(city: String) {
        mBinding.cityB.apply {
            text = city
            visibility = View.VISIBLE
        }

        mBinding.chooseCityB.visibility = View.INVISIBLE
    }

    override fun setDistanceBetweenCities(distance: String) {
        mBinding.distanceBetweenCitiesValue.text = distance
    }

    override fun setRemainingRouteDistance(distanceToB: String) {
        mBinding.distanceToBValue.text = distanceToB
    }

    override fun setArrivalTime(arrivalTime: String) {
        mBinding.timeToBValue.text = arrivalTime
    }

    override fun removeCityAName() {
        applyTransitionAnimation()
        mBinding.cityA.apply {
            text = String.empty()
            visibility = View.GONE
        }

        mBinding.chooseCityA.visibility = View.VISIBLE

        // if B city is visible button should be small and on the top
        adjustCityAButton(mBinding.cityB.visibility != View.VISIBLE)
    }

    override fun removeCityBName() {
        applyTransitionAnimation()
        mBinding.cityB.apply {
            visibility = View.GONE
            text = String.empty()
        }

        mBinding.chooseCityB.visibility = View.VISIBLE
    }

    override fun showRouteDetailsLabels() {
        Timber.i("Show all labels")
        mConstraintSet.clone(mBinding.rcvRootLayout)

        // Show all views related to info labels
        for (id in LABELS_IDS) {
            mConstraintSet.setVisibility(id, View.VISIBLE)
        }

        // Hide top label we don't need it anymore
        mConstraintSet.apply {
            setVisibility(R.id.label, View.GONE)
            setVerticalBias(mBinding.cityA.id, CITY_LABEL_TOP_BIAS)
            setVerticalBias(mBinding.cityB.id, CITY_LABEL_TOP_BIAS)
            applyTo(mBinding.rcvRootLayout)
        }

        // City A button is still visible minimize it
        adjustCityAButton(false)
    }

    override fun hideAllLabels() {
        applyTransitionAnimation()

        mConstraintSet.clone(mBinding.rcvRootLayout)
        for (id in LABELS_IDS) {
            mConstraintSet.setVisibility(id, View.GONE)
        }

        // Move label back to default position
        mConstraintSet.apply {
            setVerticalBias(mBinding.cityA.id, CITY_LABEL_DEF_BIAS)
            setVerticalBias(mBinding.cityB.id, CITY_LABEL_DEF_BIAS)
            // Restore center label
            setVisibility(R.id.label, View.VISIBLE)
            applyTo(mBinding.rcvRootLayout)
        }

        adjustCityAButton(true)
    }

    override fun needsLocationPermission(): Boolean {
        return true
    }

    override fun notifyRouteChanged(intent: Intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun pickCityA(title: Int, city: City?) {
        val intent = getPickCityIntent(title, city)
        mPickCityA.launch(intent)
        Navigation.enterZoomOutFadeIn(context as Activity)
    }

    override fun pickCityB(title: Int, city: City?) {
        val intent = getPickCityIntent(title, city)
        mPickCityB.launch(intent)
        Navigation.enterZoomOutFadeIn(context as Activity)
    }

    private fun getPickCityIntent(title: Int, city: City?): Intent {
        return Intent(context, FindCityActivity::class.java).apply {
            action = Constants.PICK_CITY_ACTION
            putExtra(Constants.TITLE_EXTRA_KEY, title)
            city?.let {
                putExtra(Constants.CITY_EXTRA_KEY, it)
            }
        }
    }

    private fun adjustCityAButton(restoreToDefaultSize: Boolean) {
        if (mBinding.chooseCityA.visibility != View.VISIBLE) {
            return
        }

        mConstraintSet.clone(mBinding.rcvRootLayout)
        val bias: Float
        val iconSize: Int

        if (restoreToDefaultSize) {
            iconSize = resources.getDimensionPixelSize(R.dimen.plane_icon_size)
            bias = PLANE_ICON_DEF_BIAS
        } else {
            iconSize = resources.getDimensionPixelSize(R.dimen.plane_icon_size_small)
            bias = PLANE_ICON_TOP_BIAS
        }

        mConstraintSet.apply {
            constrainHeight(mBinding.chooseCityA.id, iconSize)
            constrainWidth(mBinding.chooseCityA.id, iconSize)
            setVerticalBias(mBinding.chooseCityA.id, bias)
            applyTo(mBinding.rcvRootLayout)
        }
    }

    private fun applyTransitionAnimation() {
        AutoTransition().let {
            it.duration = DURATION_MS
            it.ordering = TransitionSet.ORDERING_TOGETHER
            TransitionManager.beginDelayedTransition(mBinding.rcvRootLayout, it)
        }
    }
}
