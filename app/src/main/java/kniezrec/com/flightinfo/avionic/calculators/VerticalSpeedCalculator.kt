package kniezrec.com.flightinfo.avionic.calculators

import android.text.format.DateUtils

/**
 * Copyright by Kamil Niezrecki
 */

class VerticalSpeedCalculator {

  private var mThisHeight: Double = Double.NaN
  private var mPreviousHeight: Double = Double.NaN
  private var mThisTime: Long = -1L
  private var mPreviousTime: Long = -1L

  private val mAverage = AverageCalculator(TREND_ARRAY_SIZE)

  companion object Const {
    const val TREND_ARRAY_SIZE = 3
    const val FEET_PER_MIN_FACTOR = 196.850394
    const val METER_PER_MINUTE_FACTOR = 60
  }

  fun getVerticalSpeed(unitValue: String, height: Double, time: Long): String {
    return when (unitValue) {
      "1"  -> "%+.1f m/s".format(toMetersPerSecond(height, time))

      "2"  -> "%+.1f m/min".format(toMetersPerMinute(height, time))

      "3"  -> "%+.1f ft/min".format(toFeetPerMinute(height, time))

      else -> throw IllegalArgumentException("Value not supported")
    }
  }

  internal fun toMetersPerMinute(height: Double, time: Long):
      Double = getVSInMeterPerSecondWithTrend(height, time) * METER_PER_MINUTE_FACTOR

  internal fun toFeetPerMinute(height: Double, time: Long):
      Double = (getVSInMeterPerSecondWithTrend(height, time) * FEET_PER_MIN_FACTOR) / 1000

  internal fun toMetersPerSecond(height: Double, time: Long):
      Double = getVSInMeterPerSecondWithTrend(height, time)

  internal fun isDataCorrect(): Boolean {
    return (!mPreviousHeight.isNaN() && mPreviousTime != -1L)
  }

  private fun getVSInMeterPerSecondWithTrend(height: Double, time: Long): Double {
    val verticalSpeed = getVSInMeterPerSecond(height, time)
    return mAverage.addAndGetAverage(verticalSpeed)
  }

  internal fun getVSInMeterPerSecond(height: Double, time: Long): Double {
    mPreviousHeight = mThisHeight
    mPreviousTime = mThisTime

    mThisHeight = height
    mThisTime = time

    if (!isDataCorrect()) {
      return 0.0
    }

    val timeDiff = (mThisTime - mPreviousTime) / DateUtils.SECOND_IN_MILLIS
    val heightDiff = mThisHeight - mPreviousHeight

    return if (timeDiff > 0L) heightDiff / timeDiff else 0.0
  }
}