package kniezrec.com.flightinfo.cards.satellites

import android.content.Context
import android.graphics.Color
import android.location.GpsSatellite
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.common.empty
import kniezrec.com.flightinfo.databinding.MapCardLayoutBinding
import kniezrec.com.flightinfo.databinding.SatellitesCardLayoutBinding

/**
 * Copyright by Kamil Niezrecki
 */

class SatellitesCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<SatellitesCardViewPresenter>(context, attrs, defStyleAttr),
    SatellitesCardViewPresenter.ViewContract {

  private val mRedColor = ContextCompat.getColor(context, R.color.satellite_red)
  private val mGreenColor = ContextCompat.getColor(context, R.color.satellite_green)
  private val mBinding : SatellitesCardLayoutBinding =
    SatellitesCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    setupChart()
  }

  override fun initPresenter(): SatellitesCardViewPresenter {
    return SatellitesCardViewPresenter()
  }

  private fun setupChart() {
    val fontSize = resources.getDimensionPixelSize(R.dimen.text_small)
    val p = mBinding.satellitesChart.getPaint(Chart.PAINT_INFO)
    p.color = ContextCompat.getColor(context, R.color.text_color_dark)

    val chartDescription = Description()
    chartDescription.text = String.empty()

    mBinding.satellitesChart.apply {
      getPaint(Chart.PAINT_INFO).textSize = fontSize.toFloat()
      isHighlightPerTapEnabled = false
      isHighlightPerDragEnabled = false
      setDrawBarShadow(false)
      setDrawGridBackground(false)
      setDrawValueAboveBar(false)
      setPinchZoom(false)
      setScaleEnabled(false)
      setDrawGridBackground(false)
      isDoubleTapToZoomEnabled = false
      setFitBars(true)
      description = chartDescription
    }

    mBinding.satellitesChart.xAxis.apply {
      position = XAxis.XAxisPosition.BOTTOM
      setDrawGridLines(false)
      textColor = Color.WHITE
      granularity = 1f
    }

    mBinding.satellitesChart.axisLeft.apply {
      setLabelCount(8, true)
      axisMinimum = 1f
      setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
      spaceTop = 15f
      textColor = Color.WHITE
    }

    mBinding.satellitesChart.axisRight.isEnabled = false

    mBinding.satellitesChart.legend.let {
      if (it != null && it.isEnabled) {
        it.isEnabled = false
      }
    }

    mBinding.satellitesChart.invalidate()
  }

  override fun showOnBarChart(satellites: Iterable<GpsSatellite>?) {
    val chartColors = ArrayList<Int>()
    val yValues = ArrayList<BarEntry>()
    var connectedSatellites = 0

    satellites?.forEachIndexed { i, sat ->
      yValues.add(BarEntry(i.toFloat(), sat.snr))
      if (sat.usedInFix()) {
        connectedSatellites++
        chartColors.add(mGreenColor)
      } else {
        chartColors.add(mRedColor)
      }
    }

    if (yValues.isEmpty()) {
      mBinding.apply {
        satellitesChart.invalidate()
        noSatellitesView.show()
      }
      return
    } else {
      mBinding.noSatellitesView.hide()
    }

    val set = BarDataSet(yValues, "SNR").apply {
      colors = chartColors
    }

    val dataSets = ArrayList<IBarDataSet>()
    dataSets.add(set)

    val data = BarData(dataSets).apply {
      isHighlightEnabled = false
      barWidth = .9f
      setDrawValues(false)
    }

    mBinding.satellitesChart.let {
      it.data = data
      it.invalidate()
    }

    mBinding.satellitesLabel.text =
        resources.getQuantityString(
            R.plurals.connected_satellites,
            connectedSatellites, connectedSatellites)
  }

  override fun needsLocationPermission(): Boolean {
    return true
  }

}
