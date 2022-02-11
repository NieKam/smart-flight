package kniezrec.com.flightinfo.avionic.calculators

import android.content.Context
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlin.math.asin
import kotlin.math.atan2

/**
 * Copyright by Kamil Niezrecki
 */

class OrientationCalculator(context: Context) {

  companion object Const {
    const val R2D = 57.3f
  }

  private var mRotationValues = FloatArray(5)
  private val mWindowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

  fun getRotation(eventValues: FloatArray): FloatArray {
    mRotationValues = Filter.lowPass(eventValues, mRotationValues)

    val q = FloatArray(4)
    SensorManager.getQuaternionFromVector(q, mRotationValues)

    val angles = FloatArray(3)
    getRollPitchYaw(angles, q)

    return getAnglesForScreenOrientation(angles)
  }

  private fun getAnglesForScreenOrientation(angles: FloatArray): FloatArray {
    /*
      Final array (Portrait)
      0 - roll - Not used
      1 - pitch (Y-Translate up, down)
      2 - yaw (rotation right, left)

      Array from param
      0 = pitch
      1 = yaw
      2 = roll
     */

    val screenFixedAngles = FloatArray(3)
    when (mWindowManager.defaultDisplay.rotation) {
      Surface.ROTATION_0   -> {
        screenFixedAngles[0] = angles[2]
        screenFixedAngles[1] = angles[0]
        screenFixedAngles[2] = angles[1]
      }

      Surface.ROTATION_90  -> {
        screenFixedAngles[0] = angles[2]
        screenFixedAngles[1] = -angles[1]
        screenFixedAngles[2] = angles[0]
      }

      Surface.ROTATION_180 -> {
        screenFixedAngles[0] = angles[2]
        screenFixedAngles[1] = angles[0]
        screenFixedAngles[2] = angles[1]
      }

      Surface.ROTATION_270 -> {
        screenFixedAngles[0] = angles[2]
        screenFixedAngles[1] = angles[1]
        screenFixedAngles[2] = -angles[0]
      }
    }

    return screenFixedAngles
  }

  private fun getRollPitchYaw(rpy: FloatArray, q: FloatArray) {
    var x = ((q[0] * q[1] + q[2] * q[3]) * 2.0f).toDouble()
    var y = (1.0f - (((q[1] * q[1]) + (q[2] * q[2])) * 2.0f)).toDouble()
    rpy[0] = (atan2(x, y) * R2D).toFloat()

    val a = (((q[0] * q[2]) - (q[3] * q[1])) * 2.0f).toDouble()
    rpy[1] = (asin(a) * R2D).toFloat()

    x = (((q[0] * q[3]) + (q[1] * q[2])) * 2.0f).toDouble()
    y = (1.0f - (((q[2] * q[2]) + (q[3] * q[3])) * 2.0f)).toDouble()
    rpy[2] = (atan2(x, y) * R2D).toFloat()
  }
}