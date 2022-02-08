package kniezrec.com.flightinfo.avionic

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Copyright by Kamil Niezrecki
 *
 * Unit test for {@link Speed} class
 */
class SpeedTest {

  companion object TestParameters {
    const val METERS_PER_SECOND = 100f
    const val DELTA = 0.1f
  }

  @Test
  fun testKilometerPerHourConversion() {
    val expectedSpeedInKmh = 360.0f
    val actualSpeed = Speed.toKMH(METERS_PER_SECOND)
    assertEquals(expectedSpeedInKmh, actualSpeed)
  }

  @Test
  fun testMilesPerHourConversion() {
    val expectedMphSpeedRounded = 223.693f
    val actualSpeed = Speed.toMPH(METERS_PER_SECOND)
    assertEquals(expectedMphSpeedRounded, actualSpeed, DELTA)
  }

  @Test
  fun testKnotsConversion() {
    val expectedKnotsSpeedRounded = 194.38444f
    val actualSpeed = Speed.toKnots(METERS_PER_SECOND)
    assertEquals(expectedKnotsSpeedRounded, actualSpeed, DELTA)
  }

}