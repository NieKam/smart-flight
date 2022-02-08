package kniezrec.com.flightinfo.avionic.calculators

/**
 * Copyright by Kamil Niezrecki
 */

class AverageCalculator(private val defaultSize: Int = 10) {

  private var mValues: DoubleArray? = null
  private var mIndex = 0

  fun addAndGetAverage(value: Double): Double {
    if (mValues == null) {
      mValues = DoubleArray(defaultSize) { value }
    }

    requireNotNull(mValues)[mIndex] = value
    mIndex = (mIndex + 1) % defaultSize
    return requireNotNull(mValues).average()
  }
}