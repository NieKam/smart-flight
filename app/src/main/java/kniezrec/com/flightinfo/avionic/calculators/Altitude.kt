package kniezrec.com.flightinfo.avionic.calculators

/**
 * Copyright by Kamil Niezrecki
 */
class Altitude {

  companion object {
    private const val METER_TO_KNOT_FACTOR = 3.2808399F

    fun getAltitude(unitValue: String, altitudeInMeters: Double): String {
      return when (unitValue) {
        "1"  -> "%.1f m".format(toMeters(altitudeInMeters))

        "2"  -> "%.1f feet".format(toFeet(altitudeInMeters))

        else -> throw IllegalArgumentException("Value not supported")
      }
    }

    private fun toMeters(altitudeInMeters: Double): Double = altitudeInMeters

    private fun toFeet(altitudeInMeters: Double): Double = altitudeInMeters * METER_TO_KNOT_FACTOR
  }
}