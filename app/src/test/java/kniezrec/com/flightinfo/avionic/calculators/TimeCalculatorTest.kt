package kniezrec.com.flightinfo.avionic.calculators

import kniezrec.com.flightinfo.db.City
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Copyright by Kamil Niezrecki
 */
class TimeCalculatorTest {

  @Test
  fun getTimeInCity() {
    val calendar = getCalendar()
    val testedObject = TimeCalculator()
    val actual = testedObject.getTimeInCity(getCity(), calendar.timeInMillis, true)
    val expectedTime = "12:30 (-4:00)"

    assertEquals(expectedTime, actual)
  }

  @Test
  fun getEstimatedArrivalTimePlusTwoHours() {
    val calendar = getCalendar()
    val testedObject = TimeCalculator()
    val remainingTime = TimeUnit.HOURS.toSeconds(2)
    val actual = testedObject.getEstimatedArrivalTime(getCity(), calendar.timeInMillis, remainingTime)
    val expectedTime = "14:30"

    assertEquals(expectedTime, actual)
  }

  @Test
  fun getEstimatedArrivalTimePlusTwoAndHalfHour() {
    val calendar = getCalendar()
    val testedObject = TimeCalculator()
    val remainingTime = TimeUnit.HOURS.toSeconds(2) + TimeUnit.MINUTES.toSeconds(30)
    val actual = testedObject.getEstimatedArrivalTime(getCity(), calendar.timeInMillis, remainingTime)
    val expectedTime = "15:00"

    assertEquals(expectedTime, actual)
  }

  @Test
  fun getTimeInCityPlusOneHour() {
    val calendar = getCalendar()
    val testedObject = TimeCalculator()
    val actual = testedObject.getTimeInCity(getCity(), calendar.timeInMillis, false)
    val expectedTime = "12:30 (-4:00)"

    assertEquals(expectedTime, actual)
  }


  private fun getCalendar() : Calendar {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 30)
    return calendar
  }

  private fun getCity(): City {
    // Return GMT for New York
    return City(
        0,
        "New York City",
        "United States",
        "America/New_York",
        -5.0f,
        40.71427,
        -74.00597)
  }
}