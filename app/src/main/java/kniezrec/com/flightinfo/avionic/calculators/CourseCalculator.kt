package kniezrec.com.flightinfo.avionic.calculators

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import kniezrec.com.flightinfo.avionic.Course
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Copyright by Kamil Niezrecki
 */
class CourseCalculator {

  private val mMiddlePoints: IntArray = intArrayOf(0, 45, 90, 135, 180, 225, 270, 315, 360)
  private val mAbbreviations = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")

  companion object Const {
    private const val ALPHA = 0.97f
    private const val SIZE = 3
    private const val X: Int = 0
    private const val Y: Int = 1
    private const val Z: Int = 2
  }

  private val mGravity = FloatArray(SIZE)
  private val mGeomagnetic = FloatArray(SIZE)
  private val mAverage = AverageCalculator()

  internal fun getAbbreviation(point: Int): String {
    val item: Int = mMiddlePoints.minByOrNull { abs(it - point) } ?: 0
    return mAbbreviations[mMiddlePoints.indexOf(item)]
  }

  fun getCourse(event: SensorEvent): Course {
    synchronized(this) {
      if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
        mGravity[X] = ALPHA * mGravity[X] + (Y - ALPHA) * event.values[X]
        mGravity[Y] = ALPHA * mGravity[Y] + (Y - ALPHA) * event.values[Y]
        mGravity[Z] = ALPHA * mGravity[Z] + (Y - ALPHA) * event.values[Z]
      }

      if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
        mGeomagnetic[X] = ALPHA * mGeomagnetic[X] + (1 - ALPHA) * event.values[X]
        mGeomagnetic[Y] = ALPHA * mGeomagnetic[Y] + (1 - ALPHA) * event.values[Y]
        mGeomagnetic[Z] = ALPHA * mGeomagnetic[Z] + (1 - ALPHA) * event.values[Z]
      }

      val R = FloatArray(SIZE * SIZE)
      val I = FloatArray(SIZE * SIZE)
      val isValid = SensorManager.getRotationMatrix(
          R, I, mGravity,
          mGeomagnetic)

      return if (isValid) {
        val orientation = FloatArray(SIZE)
        SensorManager.getOrientation(R, orientation)
        var azimuth = Math.toDegrees(orientation[0].toDouble())
        azimuth = mAverage.addAndGetAverage(azimuth)

        val azimuthRound = (azimuth).roundToInt()
        val azimuthNormalized = (azimuthRound + 360) % 360

        val abbreviation = getAbbreviation(azimuthNormalized)
        Course(azimuthRound, azimuthNormalized, abbreviation)
      } else {
        Course(0, 0, mAbbreviations[0])
      }
    }
  }
}