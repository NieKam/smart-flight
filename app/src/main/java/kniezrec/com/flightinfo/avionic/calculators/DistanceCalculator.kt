package kniezrec.com.flightinfo.avionic.calculators

import android.location.Location
import kniezrec.com.flightinfo.db.City
import java.util.concurrent.TimeUnit

/**
 * Copyright by Kamil Niezrecki
 */
class DistanceCalculator {

  companion object {

    private const val METERS_TO_MILES_FACTOR = 0.000621371192
    private const val METERS_TO_KILOMETERS_FACTOR = 0.001
    private const val ZERO_SPEED_TIME_FORMAT = "-:- (∞)"
    private const val TOO_MANY_HOURS_TO_DISPLAY = "..."

    private fun toKilometers(meters: Float): Double = meters * METERS_TO_KILOMETERS_FACTOR

    private fun toMiles(meters: Float): Double = meters * METERS_TO_MILES_FACTOR

    fun getDistance(distanceUnit: String, locationA: Location, locationB: Location): String {
      return getDistance(
          distanceUnit,
          locationA.latitude,
          locationA.longitude,
          locationB.latitude,
          locationB.longitude)
    }

    fun getDistance(distanceUnit: String, cityA: City, cityB: City): String {
      return getDistance(
          distanceUnit,
          cityA.latitude,
          cityA.longitude,
          cityB.latitude,
          cityB.longitude)
    }

    fun getDistance(distanceUnit: String, location: Location, city: City): String {
      return getDistance(
          distanceUnit,
          location.latitude,
          location.longitude,
          city.latitude,
          city.longitude)
    }

    fun getDistance(
        distanceUnit: String, latitudeA: Double, longitudeA: Double,
        latitudeB: Double, longitudeB: Double
    ): String {
      return when (distanceUnit) {
        "1" -> "%.1f km".format(toKilometers(latitudeA, longitudeA, latitudeB, longitudeB))

        "2" -> "%.1f mil".format(toMiles(latitudeA, longitudeA, latitudeB, longitudeB))

        else -> throw IllegalArgumentException("Value not supported")
      }
    }

    private fun toKilometers(
        latitudeA: Double, longitudeA: Double,
        latitudeB: Double, longitudeB: Double
    ): Double = toKilometers(getDistanceInMeters(latitudeA, longitudeA, latitudeB, longitudeB))

    private fun toMiles(
        latitudeA: Double, longitudeA: Double,
        latitudeB: Double, longitudeB: Double
    ): Double = toMiles(getDistanceInMeters(latitudeA, longitudeA, latitudeB, longitudeB))

    private fun getDistanceInMeters(location: Location, city: City): Float {
      return getDistanceInMeters(
          location.latitude,
          location.longitude,
          city.latitude,
          city.longitude)
    }

    private fun getDistanceInMeters(
        latitudeA: Double, longitudeA: Double,
        latitudeB: Double, longitudeB: Double
    ): Float {

      val results = FloatArray(3)
      Location.distanceBetween(latitudeA, longitudeA, latitudeB, longitudeB, results)
      return results[0]
    }

    fun getEstimatedTimeToLocation(location: Location, city: City): String {
      val speedMpS = location.speed
      if (speedMpS <= 0) {
        return ZERO_SPEED_TIME_FORMAT
      }

      val distanceInMeters = getDistanceInMeters(location, city)
      val timeInSeconds = (distanceInMeters / speedMpS).toLong()

      val hour = TimeUnit.SECONDS.toHours(timeInSeconds)
      val minute = TimeUnit.SECONDS.toMinutes(timeInSeconds) - TimeUnit.SECONDS.toHours(
          timeInSeconds) * 60

      val remainingTime: String

      remainingTime = if (timeInSeconds <= TimeUnit.DAYS.toSeconds(
              1
          )) {
        if (hour == 0L) {
          "${minute}m"
        } else {
          "${hour}h ${minute}min"
        }
      } else {
        TOO_MANY_HOURS_TO_DISPLAY
      }

      val timeCalculator = TimeCalculator()
      val currentTimeInDestination = timeCalculator.getEstimatedArrivalTime(city, timeInSeconds)

      return "$currentTimeInDestination ($remainingTime)"
    }

    fun getZero(distanceUnit: String): String {
      return getDistance(distanceUnit, 0.0, 0.0, 0.0, 0.0)
    }
  }
}