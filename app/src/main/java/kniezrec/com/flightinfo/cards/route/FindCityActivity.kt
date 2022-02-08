package kniezrec.com.flightinfo.cards.route

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.base.BaseActivity
import kniezrec.com.flightinfo.common.Constants
import kniezrec.com.flightinfo.common.DialogUtils
import kniezrec.com.flightinfo.common.Navigation
import kniezrec.com.flightinfo.common.setCustomMarker
import kniezrec.com.flightinfo.databinding.ActivityFindCityBinding
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import kniezrec.com.flightinfo.settings.FlightAppPreferences
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Copyright by Kamil Niezrecki
 */

private const val DEFAULT_ZOOM = 3

class FindCityActivity : BaseActivity(), FindCityPresenter.ViewContract, MapEventsReceiver {
  private val mAdapter = DisambiguationAdapter(object : DisambiguationAdapter.ClickListener {
    override fun onCityClicked(city: City) {
      mPresenter.onCityClicked(city)
    }
  })

  private val mCenter = GeoPoint(0.0, -32.0)

  private lateinit var mPresenter: FindCityPresenter
  private lateinit var mInputManager: InputMethodManager
  private lateinit var mMapController: IMapController
  private lateinit var mCityMarker: Marker
  private lateinit var mBinding : ActivityFindCityBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mBinding = ActivityFindCityBinding.inflate(layoutInflater)
    setContentView(mBinding.root)

    mPresenter = FindCityPresenter(CitiesDataSource(this), FlightAppPreferences(this))

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setTitle(intent.getIntExtra(Constants.TITLE_EXTRA_KEY, R.string.app_name))
    }

    mInputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    val itemDecor = DividerItemDecoration(this, VERTICAL)
    mBinding.rvDisambiguationCities.apply {
      setHasFixedSize(true)
      adapter = mAdapter
      layoutManager = LinearLayoutManager(this@FindCityActivity)
      addItemDecoration(itemDecor)
    }

    mMapController = mBinding.mapView.controller
    mBinding.confirmButton.setOnClickListener { mPresenter.onConfirmClicked() }
    mBinding.cityNameEditText.apply {
      addTextChangedListener(mTextWatcher)
      setOnEditorActionListener(mEditorActionListener)
    }

    mBinding.searchButton.setOnClickListener { prepareCitySearch() }

    mCityMarker = Marker(mBinding.mapView)
    initMapView()
  }

  private fun initMapView() {
    mCityMarker.let {
      it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
      it.setCustomMarker(this, R.drawable.ic_city_found_marker)
    }

    mMapController.let {
      it.setCenter(mCenter)
      it.setZoom(DEFAULT_ZOOM)
    }

    val eventOverlay = MapEventsOverlay(this)
    mBinding.mapView.let {
      it.overlays.add(eventOverlay)
      it.invalidate()
    }
  }

  override fun hideDisambiguation() {
    mBinding.rvDisambiguationCities.visibility = View.GONE
  }

  override fun connectToCityFinder() {
    Navigation.bindToFindCityService(this, mFindCityServiceConnection)
  }

  override fun disconnectFromCityFinder() {
    Navigation.unbindFromService(this, mFindCityServiceConnection)
  }

  override fun onBackPressed() {
    super.onBackPressed()
    Navigation.exitZoomInFadeOut(this)
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  override fun sendResults(city: City) {
    val actionFromIntent = intent.action

    val returnIntent = Intent().apply {
      action = actionFromIntent
      putExtra(Constants.CITY_EXTRA_KEY, city)
    }

    setResult(Activity.RESULT_OK, returnIntent)
    finish()
    Navigation.exitZoomInFadeOut(this)
  }

  override fun onStart() {
    super.onStart()
    mPresenter.attachView(this)
    intent.getParcelableExtra<City>(Constants.CITY_EXTRA_KEY)?.let {
      mPresenter.onCityFound(it)
    }
  }

  override fun onStop() {
    super.onStop()
    mPresenter.detachView()
  }

  override fun onDestroy() {
    mCityMarker.onDetach(mBinding.mapView)
    mBinding.mapView.overlays?.clear()
    Marker.cleanDefaults()
    super.onDestroy()
  }

  override fun displayFoundCities(cities: List<City>) {
    mAdapter.addAll(cities)
    mBinding.rvDisambiguationCities.visibility = View.VISIBLE
    mBinding.mapView.overlays.remove(mCityMarker)
    mMapController.apply {
      setZoom(DEFAULT_ZOOM)
      animateTo(mCenter)
    }
  }

  override fun setOrientation(orientation: Int) {
    requestedOrientation = orientation
  }

  override fun displayDisambiguationInfo() {
    mBinding.infoLabel.setText(R.string.more_than_one_city_title)
  }

  override fun showNoCityFoundDialog() {
    DialogUtils.showErrorDialog(this, R.string.no_city_found_title, R.string.no_city_body)
  }

  override fun longPressHelper(point: GeoPoint?): Boolean {
    mPresenter.onLongPress(point)
    return true
  }

  override fun singleTapConfirmedHelper(p: GeoPoint?) = false

  override fun setConfirmButtonEnabled(isEnabled: Boolean) {
    mBinding.confirmButton.isEnabled = isEnabled
  }

  override fun setSearchButtonEnabled(isEnabled: Boolean) {
    mBinding.searchButton.isEnabled = isEnabled
  }

  override fun displayCity(city: City) {
    mBinding.cityNameEditText.setText(city.name)
    mBinding.infoLabel.apply {
      text = getFormattedText(city)
      setTextColor(ContextCompat.getColor(this@FindCityActivity, R.color.text_color_light))
    }

    val geoPoint = GeoPoint(city.latitude, city.longitude)
    mCityMarker.position = geoPoint
    mMapController.animateTo(geoPoint)

    mBinding.mapView.apply {
      if (overlays.contains(mCityMarker)) {
        overlays.remove(mCityMarker)
      }
    }

    mBinding.mapView.apply {
      overlays?.add(0, mCityMarker)
      invalidate()
      performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
  }

  private fun prepareCitySearch() {
    mPresenter.onSearchButtonClicked(mBinding.cityNameEditText.text.toString())
    hideKeyboard()
  }

  private fun hideKeyboard() {
    val token: IBinder = currentFocus?.windowToken ?: mBinding.mapView.windowToken ?: return
    mInputManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
  }

  private fun getFormattedText(city: City): CharSequence {
    return "${city.name}, ${city.country}"
  }

  private val mEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
      prepareCitySearch()
      true
    } else {
      false
    }
  }

  private val mFindCityServiceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {
      mPresenter.onCityFinderDisconnected()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      mPresenter.onCityFinderConnected(Messenger(service))
    }
  }

  private val mTextWatcher = object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
      mPresenter.onCityTextChanged(s)
    }

    override fun afterTextChanged(s: Editable?) {}
  }
}
