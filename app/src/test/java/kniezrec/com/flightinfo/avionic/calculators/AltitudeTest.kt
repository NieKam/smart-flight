package kniezrec.com.flightinfo.avionic.calculators

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Copyright by Kamil Niezrecki
 *
 * Unit test for {@link Altitude} class
 */
class AltitudeTest {

  companion object TestParameters {
    const val ALTITUDE_IN_METERS = 112345.0
    const val DELTA = 0.1
  }

  @Test
  fun testGetAltitudeInMeters() {
    val actualAltitude = Altitude.toMeters(ALTITUDE_IN_METERS)
    assertEquals(ALTITUDE_IN_METERS, actualAltitude, DELTA)
  }

  @Test
  fun testGetAltitudeInFeet() {
    val expectedAltitude = 368_585.958
    val actualAltitude = Altitude.toFeet(ALTITUDE_IN_METERS)
    assertEquals(expectedAltitude, actualAltitude, DELTA)
  }

}