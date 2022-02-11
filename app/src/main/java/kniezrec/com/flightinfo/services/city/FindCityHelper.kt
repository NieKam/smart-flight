package kniezrec.com.flightinfo.services.city

import android.location.Location
import android.os.Message
import android.os.Messenger
import com.infomatiq.jsi.Point
import com.infomatiq.jsi.rtree.RTree
import kniezrec.com.flightinfo.db.CitiesDataSource
import kniezrec.com.flightinfo.db.City
import timber.log.Timber

class FindCityHelper(private val dataSource: CitiesDataSource) {

    private val mSpatialIndex = RTree()
    private var mIsRTreeInitialized = false

    fun indexAllCities() {
        if (mIsRTreeInitialized) {
            return
        }
        val start = System.currentTimeMillis()

        Timber.i("Start indexing")
        mSpatialIndex.init(null)

        val cursor = dataSource.getAll()
        if (!cursor.moveToFirst()) {
            Timber.e("Error during reading cursor: ${cursor.columnCount} columns count ${cursor.count}")
            cursor.close()
            return
        }

        do {
            val point = dataSource.cursorToMapPoint(cursor)
            mSpatialIndex.add(point.rectangle, point.id)
        } while (dataSource.isOpen() && cursor.moveToNext())

        cursor.close()
        mIsRTreeInitialized = true
        Timber.i("Finish indexing in ${System.currentTimeMillis() - start}")
    }

    fun findCity(replyMessenger: Messenger, location: Location) {
        val replyMsg = Message.obtain(null, FindCityService.Contract.MSG_CITY_FOUND)

        if (!mIsRTreeInitialized) {
            replyMessenger.send(replyMsg)
            return
        }
        var city: City? = null
        val point = Point(location.latitude.toFloat(), location.longitude.toFloat())

        mSpatialIndex.nearestN(
            point, { id ->
                city = dataSource.getCityById(id)
                true
            }, 1, Float.MAX_VALUE
        )

        replyMsg.obj = city
        replyMessenger.send(replyMsg)
    }
}