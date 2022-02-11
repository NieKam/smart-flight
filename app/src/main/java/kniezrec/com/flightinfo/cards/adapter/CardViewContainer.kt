package kniezrec.com.flightinfo.cards.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.CanChangeVisibility
import kniezrec.com.flightinfo.cards.base.CardItem
import kniezrec.com.flightinfo.cards.course.CourseCardView
import kniezrec.com.flightinfo.cards.gps.FlightParametersCardView
import kniezrec.com.flightinfo.cards.gps.LocationCardView
import kniezrec.com.flightinfo.cards.horizon.HorizonCardView
import kniezrec.com.flightinfo.cards.map.MapCardView
import kniezrec.com.flightinfo.cards.permission.PermissionCardView
import kniezrec.com.flightinfo.cards.permission.PermissionManager
import kniezrec.com.flightinfo.cards.route.RouteCardView
import kniezrec.com.flightinfo.cards.satellites.SatellitesCardView
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */
class CardViewContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var mPermissionCardView: PermissionCardView? = null

  private val mCardRadius = resources.getDimensionPixelSize(R.dimen.card_radius).toFloat()
  private val mCardElevation = resources.getDimensionPixelSize(R.dimen.card_elevation).toFloat()
  private val mCardBackgroundColor = ContextCompat.getColor(context, R.color.card_background)
  private val mCardMargin = resources.getDimensionPixelSize(R.dimen.card_margin)

  private val mCardsItems: List<CardItem>
  private val mRouteCardView: RouteCardView = RouteCardView(context)

  init {
    orientation = VERTICAL
    val prefs = FlightAppPreferences(context)
    val showCourseCard = !prefs.isCourseCardHidden()
    val showHorizonCard = !prefs.isHorizonCardHidden()

    val hasLocationPermission = PermissionManager.hasLocationPermission(context)

    if (!hasLocationPermission) {
      mPermissionCardView = PermissionCardView(context)
      addCard(requireNotNull(mPermissionCardView))
    }

    mCardsItems = listOfNotNull(
        if (showCourseCard) CourseCardView(context) else null,
        if (showHorizonCard) HorizonCardView(context) else null,
        SatellitesCardView(context),
        FlightParametersCardView(context),
        LocationCardView(context),
        mRouteCardView,
        MapCardView(context)

    )

    addCards(mCardsItems, hasLocationPermission)
  }

  fun onPermissionGranted() {
    TransitionManager.beginDelayedTransition(this)
    reInitLayout(true)
  }

  fun onPermissionDeniedPermanently() {
    mPermissionCardView?.onPermissionDeniedPermanently()
  }

  fun refreshView() {
    // Check if permission was granted manually in settings
    val hasLocationPermission = PermissionManager.hasLocationPermission(context)
    if (indexOfChild(mPermissionCardView) != -1 && hasLocationPermission) {
      reInitLayout(true)
    }
  }

  private fun reInitLayout(hasPermission: Boolean) {
    removeAllViews()
    addCards(mCardsItems, hasPermission)
    mPermissionCardView = null
  }

  private fun addCards(cards: List<CardItem>, hasPermission: Boolean) {
    cards.forEach {
      val cardView = it as CardView
      if (it.needsLocationPermission()) {
        if (hasPermission) {
          addCard(cardView)
        }
      } else {
        addCard(cardView)
      }
    }
  }

  private fun addCard(cardView: CardView) {
    if (cardView is CanChangeVisibility) {
      cardView.registerOnVisibilityChangedListener(object :
          CanChangeVisibility.OnVisibilityChangedListener {
        override fun onVisibilityChanged(isVisible: Boolean) {
          TransitionManager.beginDelayedTransition(this@CardViewContainer)
          removeView(cardView)
        }
      })
    }

    cardView.apply {
      setCardBackgroundColor(mCardBackgroundColor)
      radius = mCardRadius
      cardElevation = mCardElevation
    }

    addView(cardView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    (cardView.layoutParams as LayoutParams).apply {
      setMargins(mCardMargin, mCardMargin, mCardMargin, 0)
    }
  }
}