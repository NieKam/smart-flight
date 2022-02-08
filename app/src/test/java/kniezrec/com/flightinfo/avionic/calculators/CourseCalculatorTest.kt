package kniezrec.com.flightinfo.avionic.calculators

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Copyright by Kamil Niezrecki
 */

class CourseCalculatorTest {

  @Test
  fun test_get_abbreviation() {
    val testedObject = CourseCalculator()
    val expected = arrayOf("NE", "E", "SE", "S", "SW", "W", "NW")

    (0..22)
        .map { testedObject.getAbbreviation(it) }
        .forEach { assertEquals("N", it) }

    (338..360)
        .map { testedObject.getAbbreviation(it) }
        .forEach { assertEquals("N", it) }


    var startValue = 23
    expected.forEach { _expected ->
      (0..44)
          .map { testedObject.getAbbreviation(startValue) }
          .forEach {
            assertEquals(_expected, it)
            startValue = startValue.inc()
          }
    }
  }
}