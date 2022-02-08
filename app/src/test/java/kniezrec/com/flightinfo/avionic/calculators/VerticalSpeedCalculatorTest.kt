package kniezrec.com.flightinfo.avionic.calculators

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Copyright by Kamil Niezrecki
 *
 * Unit test for {@link VerticalSpeedCalculator} class
 */
class VerticalSpeedCalculatorTest {
  private lateinit var mTestedObject: VerticalSpeedCalculator

  companion object TestParameters {
    const val DELTA = 0.1
  }

  @Before
  fun setUp() {
    mTestedObject = VerticalSpeedCalculator()
  }

  @Test
  fun test_is_data_correct_should_return_false() {
    assertFalse(mTestedObject.isDataCorrect())

    mTestedObject.getVSInMeterPerSecond(0.0, 1)

    assertFalse(mTestedObject.isDataCorrect())
  }

  @Test
  fun test_is_data_correct_should_return_true() {
    mTestedObject.getVSInMeterPerSecond(0.0, 1)
    mTestedObject.getVSInMeterPerSecond(0.0, 2)

    assertTrue(mTestedObject.isDataCorrect())
  }

  @Test
  fun test_vertical_speed_when_altitude_is_constants() {
    var startTimeInMillis = System.currentTimeMillis()
    val startAltitudeInMeters = 100.0

    val expected = 0.0
    var actual: Double
    for (i in 0..50) {
      actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
      assertEquals(expected, actual, DELTA)

      // add one second in millis
      startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
    }
  }

  @Test
  fun test_vertical_speed_when_altitude_is_increasing() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 100.0

    val expected = 5.0
    var actual: Double


    for (i in 0..50) {
      actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
      if (mTestedObject.isDataCorrect()) {
        assertEquals(expected, actual, DELTA)
      }

      startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
      startAltitudeInMeters += 5.0
    }
  }

  @Test
  fun test_vertical_speed_when_altitude_is_decreasing() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 1000000.0

    val expected = -5.0
    var actual: Double


    for (it in 0..50) {
      actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
      if (mTestedObject.isDataCorrect()) {
        assertEquals(expected, actual, DELTA)
      }

      startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
      startAltitudeInMeters -= 5.0
    }
  }

  @Test
  fun test_vertical_speed_when_altitude_is_decreasing_and_increasing() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 1000000.0

    mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
    mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)

    var expected = 5.0
    var actual: Double

    // add 10 meters after two seconds / average should be 5m per second
    startTimeInMillis += TimeUnit.SECONDS.toMillis(2)
    startAltitudeInMeters += 10.0

    actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)

    assertEquals(expected, actual, DELTA)

    // Remove 30 meters in 3 second / average should be  - 10m per second
    startTimeInMillis += TimeUnit.SECONDS.toMillis(3)
    startAltitudeInMeters -= 30.0

    actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
    expected = -10.0

    assertEquals(expected, actual, DELTA)

    // add one meter in 1 second / average should be 1m per second
    startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
    startAltitudeInMeters += 1

    actual = mTestedObject.getVSInMeterPerSecond(startAltitudeInMeters, startTimeInMillis)
    expected = 1.0

    assertEquals(expected, actual, DELTA)
  }

  // Test conversion

  @Test
  fun testToMetersPerSecond() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 1000000.0

    mTestedObject.toMetersPerSecond(startAltitudeInMeters, startTimeInMillis)
    mTestedObject.toMetersPerSecond(startAltitudeInMeters, startTimeInMillis)

    // Simulate 10 meters per second
    startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
    startAltitudeInMeters += 12.0

    val actual = mTestedObject.toMetersPerSecond(startAltitudeInMeters, startTimeInMillis)

    // Expected 2 meters because conversions use average and average of {0,0,12} is 4
    val expected = 4.0
    assertEquals(expected, actual, DELTA)
  }

  @Test
  fun testToFeetPerMinute() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 100.0

    mTestedObject.toFeetPerMinute(startAltitudeInMeters, startTimeInMillis)
    mTestedObject.toFeetPerMinute(startAltitudeInMeters, startTimeInMillis)

    // Simulate 10 meters per second and average is 2.
    startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
    startAltitudeInMeters += 12.0

    val actual = mTestedObject.toFeetPerMinute(startAltitudeInMeters, startTimeInMillis) * 100
    val expected = 787.40

    assertEquals(expected, actual, DELTA)
  }

  @Test
  fun testToMetersPerMinute() {
    var startTimeInMillis = System.currentTimeMillis()
    var startAltitudeInMeters = 100.0

    mTestedObject.toMetersPerMinute(startAltitudeInMeters, startTimeInMillis)
    mTestedObject.toMetersPerMinute(startAltitudeInMeters, startTimeInMillis)

    // Simulate 10 meters per second, average results is 2
    startTimeInMillis += TimeUnit.SECONDS.toMillis(1)
    startAltitudeInMeters += 12.0

    val actual = mTestedObject.toMetersPerMinute(startAltitudeInMeters, startTimeInMillis)
    val expected = 240.0

    assertEquals(expected, actual, DELTA)
  }
}