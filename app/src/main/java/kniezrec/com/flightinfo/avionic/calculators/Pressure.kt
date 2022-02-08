package kniezrec.com.flightinfo.avionic.calculators

/**
 * Copyright by Kamil Niezrecki
 */
private const val MBAR_TO_INHG_RATIO = 0.02953

class Pressure {

  companion object {
    fun getPressure(unitValue: String, pressure: Float): String {
      return when (unitValue) {
        "1"  -> toMbar(pressure)

        "2"  -> toInHg(pressure)

        else -> throw IllegalArgumentException("Value not supported")
      }
    }

    private fun toMbar(pressure: Float): String = "%.1f mbar".format(pressure)

    private fun toInHg(pressure: Float): String {
      val pressureInInchHg = pressure * MBAR_TO_INHG_RATIO
      return "%.1f inHg".format(pressureInInchHg)
    }
  }
}