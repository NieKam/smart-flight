package kniezrec.com.flightinfo.avionic.calculators

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Copyright by Kamil Niezrecki
 *
 * Unit test for {@link AverageCalculator} class
 */
class AverageCalculatorTest {
  private lateinit var mTestedObject: AverageCalculator

  companion object TestParameters {
    const val DEFAULT_VALUE = 1.0
    const val DELTA = 0.1
  }

  @Before
  fun setUp() {
    mTestedObject = AverageCalculator()
  }

  @Test
  fun test_average_with_one_value() {
    val actual = mTestedObject.addAndGetAverage(DEFAULT_VALUE)
    assertEquals(DEFAULT_VALUE, actual, DELTA)
  }

  @Test
  fun test_average_increase_with_default_size() {
    var startValue = 1.0
    val incrementConst = 9.9

    // init with 1
    mTestedObject.addAndGetAverage(DEFAULT_VALUE)

    // put 100 ten times, average should increase by 9.9 in every iteration
    for (i in 1..10) {
      val actualAverage = mTestedObject.addAndGetAverage(100.0)
      startValue += incrementConst

      val expected = startValue
      assertEquals(expected, actualAverage, DELTA)
    }

    // Check if average is still 100
    val actualAverage = mTestedObject.addAndGetAverage(100.0)
    val expected = 100.0
    assertEquals(expected, actualAverage, DELTA)
  }

  @Test
  fun test_average_decrease_with_default_size() {
    // init with 1
    mTestedObject.addAndGetAverage(100.0)

    // Check if average decrease
    val actualAverage = mTestedObject.addAndGetAverage(1.0)
    val expected = 90.1
    assertEquals(expected, actualAverage, DELTA)
  }

  @Test
  fun test_average_increase_with_non_default_size() {
    mTestedObject = AverageCalculator(5)
    // init with 1
    mTestedObject.addAndGetAverage(DEFAULT_VALUE)

    var startValue = 1.0
    val incrementConst = 19.8

    // put 100 five times, average should increase by 19.8 in every iteration
    for (i in 1..5) {
      val actualAverage = mTestedObject.addAndGetAverage(100.0)
      startValue += incrementConst

      val expected = startValue
      assertEquals(expected, actualAverage, DELTA)
    }

    // Check if average is still 100
    val actualAverage = mTestedObject.addAndGetAverage(100.0)
    val expected = 100.0
    assertEquals(expected, actualAverage, DELTA)
  }
}