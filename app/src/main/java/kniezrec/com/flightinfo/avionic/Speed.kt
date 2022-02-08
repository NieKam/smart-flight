package kniezrec.com.flightinfo.avionic

/**
 * Copyright by Kamil Niezrecki
 */
class Speed {

  companion object {

    private const val MPS_TO_KMH_FACTOR = 3.6F
    private const val MPS_TO_MPH_FACTOR = 2.23694F
    private const val MPS_TO_KNOTS_FACTOR = 1.94384449F

    fun getSpeed(unitValue: String, metersPerSecond: Float): String {
      return when (unitValue) {
        "1" -> "%.1f km/h".format(toKMH(metersPerSecond))

        "2" -> "%.1f mph".format(toMPH(metersPerSecond))

        "3" -> "%.1f kt".format(toKnots(metersPerSecond))

        else -> throw IllegalArgumentException("Value not supported")
      }
    }

    private fun toKMH(metersPerSecond: Float): Float = metersPerSecond * MPS_TO_KMH_FACTOR

    private fun toMPH(metersPerSecond: Float): Float = metersPerSecond * MPS_TO_MPH_FACTOR

    private fun toKnots(metersPerSecond: Float): Float = metersPerSecond * MPS_TO_KNOTS_FACTOR
  }
}