package kniezrec.com.flightinfo.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kniezrec.com.flightinfo.avionic.Course
import kniezrec.com.flightinfo.avionic.calculators.CourseCalculator
import kniezrec.com.flightinfo.avionic.calculators.OrientationCalculator
import kniezrec.com.flightinfo.common.Constants
import timber.log.Timber

class SensorService : Service(), SensorEventListener {
  interface CourseCallback {
    fun onCourseFixed(course: Course)
  }

  interface PressureCallback {
    fun onPressure(pressure: Float)
  }

  interface RotationCallback {
    fun onRotation(roll: Float, pitch: Float, yaw: Float)
  }

  private val mCourseCallbackClients: HashSet<CourseCallback> = HashSet()
  private val mPressureCallbackClients: HashSet<PressureCallback> = HashSet()
  private val mRotationCallbackClients: HashSet<RotationCallback> = HashSet()
  private val mBinder: IBinder = LocalBinder()
  private val mCourseCalculator = CourseCalculator()

  private lateinit var mOrientationCalculator: OrientationCalculator

  private var mSensorManager: SensorManager? = null
  private var mGForceSensor: Sensor? = null
  private var mRotationSensor: Sensor? = null
  private var mMagneticSensor: Sensor? = null
  private var mPressureSensor: Sensor? = null
  private var mIsServiceListening = false

  inner class LocalBinder : Binder() {
    fun getService(): SensorService {
      return this@SensorService
    }
  }

  override fun onCreate() {
    super.onCreate()
    Timber.i("SensorService: onCreate")
    mOrientationCalculator = OrientationCalculator(this)
    mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    mSensorManager?.let {
      mGForceSensor = it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      mMagneticSensor = it.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
      mPressureSensor = it.getDefaultSensor(Sensor.TYPE_PRESSURE)
      mRotationSensor = it.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    val intentFilter = IntentFilter(Constants.APP_LIFECYCLE_ACTION)
    LocalBroadcastManager.getInstance(this).registerReceiver(
        mAppStateBroadcastReceiver,
        intentFilter)
    startSensorListening()
  }

  override fun onDestroy() {
    stopSensorListening()
    Timber.i("onDestroy()")
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mAppStateBroadcastReceiver)
    super.onDestroy()
  }

  override fun onBind(intent: Intent): IBinder {
    return mBinder
  }

  fun addCourseCallbackClient(callback: CourseCallback) {
    mCourseCallbackClients.add(callback)
    Timber.i("Added CourseCallback callback, total items %d", mCourseCallbackClients.size)
  }

  fun removeCourseCallbackClient(callback: CourseCallback) {
    mCourseCallbackClients.remove(callback)
    Timber.i("Removed CourseCallback callback, total items %d", mCourseCallbackClients.size)
  }

  fun addPressureCallbackClient(callback: PressureCallback) {
    mPressureCallbackClients.add(callback)
    Timber.i("Added PressureCallback callback, total items %d", mPressureCallbackClients.size)
  }

  fun removePressureCallbackClient(callback: PressureCallback) {
    mPressureCallbackClients.remove(callback)
    Timber.i("Removed PressureCallback callback, total items %d", mPressureCallbackClients.size)
  }

  fun addRotationCallbackClient(callback: RotationCallback) {
    mRotationCallbackClients.add(callback)
    Timber.i("Added Rotation callback, total items %d", mRotationCallbackClients.size)
  }

  fun removeRotationCallbackClient(callback: RotationCallback) {
    mRotationCallbackClients.remove(callback)
    Timber.i("Removed Rotation callback, total items %d", mRotationCallbackClients.size)
  }

  private fun startSensorListening() {
    if (mIsServiceListening) {
      Timber.i("Currently listening, ignoring...")
      return
    }

    Timber.i("Start listening for sensor events.")
    mSensorManager?.let {
      it.registerListener(this, mGForceSensor, SensorManager.SENSOR_DELAY_GAME)
      it.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME)
      it.registerListener(this, mPressureSensor, SensorManager.SENSOR_DELAY_GAME)
      it.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_GAME)
    }
    mIsServiceListening = true
  }

  private fun stopSensorListening() {
    if (!mIsServiceListening) {
      Timber.i("Currently not listening, ignoring...")
      return
    }

    mSensorManager?.unregisterListener(this)
    mIsServiceListening = false
    Timber.i("Stop listening for sensor events")
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
  }

  override fun onSensorChanged(event: SensorEvent) {
    when (event.sensor.type) {
      Sensor.TYPE_PRESSURE        -> {
        val millibarsOfPressure = event.values[0]
        for (client in mPressureCallbackClients) {
          client.onPressure(millibarsOfPressure)
        }
      }

      Sensor.TYPE_ROTATION_VECTOR -> {
        val angles = mOrientationCalculator.getRotation(event.values.clone())
        for (client in mRotationCallbackClients) {
          client.onRotation(angles[0], angles[1], angles[2])
        }
      }

      Sensor.TYPE_ACCELEROMETER,
      Sensor.TYPE_MAGNETIC_FIELD  -> {
        val course = mCourseCalculator.getCourse(event)
        for (client in mCourseCallbackClients) {
          client.onCourseFixed(course)
        }
      }
    }
  }

  private val mAppStateBroadcastReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (action != Constants.APP_LIFECYCLE_ACTION) {
        return
      }

      val isAppForeground = requireNotNull(intent.extras).getBoolean(Constants.ON_APP_FOREGROUND_KEY)
      Timber.i("Received Intent Action: %s, is app resumed %b", action, isAppForeground)

      if (isAppForeground) {
        startSensorListening()
      } else {
        stopSensorListening()
      }
    }
  }
}
