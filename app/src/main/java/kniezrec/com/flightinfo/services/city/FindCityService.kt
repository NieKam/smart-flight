package kniezrec.com.flightinfo.services.city

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.*
import kniezrec.com.flightinfo.db.CitiesDataSource
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * Copyright by Kamil Niezrecki
 */
class FindCityService : Service() {

    object Contract {
        const val MSG_INDEX_ALL = 1
        const val MSG_FIND_CITY = 2
        const val MSG_CITY_FOUND = 3
    }

    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: FindCityHandler
    private lateinit var mMessenger: Messenger
    private lateinit var mThread: HandlerThread
    private lateinit var mFindCityHelper: FindCityHelper

    override fun onCreate() {
        super.onCreate()
        mThread = HandlerThread("ServiceThread", Process.THREAD_PRIORITY_BACKGROUND).also {
            it.start()
        }
        mServiceLooper = mThread.looper
        mFindCityHelper = FindCityHelper(CitiesDataSource(applicationContext))
        mServiceHandler = FindCityHandler(mServiceLooper, WeakReference(mFindCityHelper))
        mMessenger = Messenger(mServiceHandler)
        mServiceHandler.obtainMessage(Contract.MSG_INDEX_ALL, null).sendToTarget()
    }

    override fun onBind(intent: Intent): IBinder {
        return mMessenger.binder
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        mThread.quit()
        mServiceHandler.apply {
            removeCallbacksAndMessages(null)
            looper.quit()
        }
        super.onDestroy()
    }

    private class FindCityHandler(looper: Looper, private val helperRef: WeakReference<FindCityHelper>) :
        Handler(looper) {

        override fun handleMessage(msg: Message) {
            Timber.i("handleMessage received: ${msg.what}")
            val helper = helperRef.get() ?: return

            when (msg.what) {
                Contract.MSG_FIND_CITY -> {
                    val location = msg.obj as Location
                    helper.findCity(msg.replyTo, location)
                }

                Contract.MSG_INDEX_ALL -> {
                    helper.indexAllCities()
                }
            }
        }
    }
}
