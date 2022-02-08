package kniezrec.com.flightinfo.avionic.calculators

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Copyright by Kamil Niezrecki
 */

class DistanceCalculatorTest {

  companion object TestParameters {
    const val DISTANCE_IN_METERS = 2000f
    const val DELTA = 0.1
  }

  @Test
  fun convert_to_km_test() {
    val expected = 2.0
    val actual = DistanceCalculator.toKilometers(DISTANCE_IN_METERS)
    assertEquals(expected, actual, DELTA)
  }

  @Test
  fun convert_to_miles_test() {
    val expected = 1.24274
    val actual = DistanceCalculator.toMiles(DISTANCE_IN_METERS)
    assertEquals(expected, actual, DELTA)
  }
}