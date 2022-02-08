package kniezrec.com.flightinfo.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.*
import com.infomatiq.jsi.Point
import com.infomatiq.jsi.rtree.RTree
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class FindCityService : Service(), Handler.Callback {

    object Contract {
        const val MSG_INDEX_ALL = 1
        const val MSG_FIND_CITY = 2
        const val MSG_CITY_FOUND = 3
    }

    private val mSpatialIndex = RTree()
    private val mDataSource: CitiesDataSource by lazy {
        CitiesDataSource(applicationContext)
    }

    private lateinit var mServiceLooper: Looper
    private lateinit var mServiceHandler: Handler
    private lateinit var mMessenger: Messenger

    private var mIsRTreeInitialized = false

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("ServiceThread", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        mServiceLooper = thread.looper
        mServiceHandler = Handler(mServiceLooper, this)
        mMessenger = Messenger(mServiceHandler)
        mServiceHandler.obtainMessage(Contract.MSG_INDEX_ALL, null).sendToTarget()
    }

    override fun onBind(intent: Intent): IBinder {
        return mMessenger.binder
    }

    override fun onDestroy() {
        mServiceHandler.looper.quit()
        super.onDestroy()
    }

    override fun handleMessage(msg: Message): Boolean {
        Timber.i("handleMessage received: ${msg.what}")

        return when (msg.what) {
            Contract.MSG_FIND_CITY -> {
                val location = msg.obj as Location
                findCity(msg.replyTo, location)
                true
            }
            Contract.MSG_INDEX_ALL -> {
                indexAllCities()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun indexAllCities() {
        if (mIsRTreeInitialized) {
            return
        }
        val start = System.currentTimeMillis()

        Timber.i("Start indexing")
        mSpatialIndex.init(null)

        val cursor = mDataSource.getAll()
        if (!cursor.moveToFirst()) {
            Timber.e("Error during reading cursor: ${cursor.columnCount} columns count ${cursor.count}")
            cursor.close()
            return
        }

        do {
            val point = mDataSource.cursorToMapPoint(cursor)
            mSpatialIndex.add(point.rectangle, point.id)
        } while (mDataSource.isOpen() && cursor.moveToNext())

        cursor.close()
        mIsRTreeInitialized = true
        Timber.i("Finish indexing in ${System.currentTimeMillis() - start}")
    }

    private fun findCity(replyMessenger: Messenger, location: Location) {
        val replyMsg = Message.obtain(null, Contract.MSG_CITY_FOUND)
        if (!mIsRTreeInitialized) {
            replyMessenger.send(replyMsg)
            return
        }
        var city: City? = null
        val point = Point(location.latitude.toFloat(), location.longitude.toFloat())

        mSpatialIndex.nearestN(
            point, { id ->
                city = mDataSource.getCityById(id)
                true
            }, 1, Float.MAX_VALUE
        )

        replyMsg.obj = city
        replyMessenger.send(replyMsg)
    }
}
