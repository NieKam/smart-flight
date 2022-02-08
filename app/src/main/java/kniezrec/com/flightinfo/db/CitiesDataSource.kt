package kniezrec.com.flightinfo.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.infomatiq.jsi.Rectangle
import kniezrec.com.flightinfo.common.requireNonUi
import kniezrec.com.flightinfo.startup.SmartFlightApplication

/**
 * Copyright by Kamil Niezrecki
 */
class CitiesDataSource(context: Context) {

  private val mDbHelper = (context.applicationContext as SmartFlightApplication).getDbHelper()
  private val mDatabase: SQLiteDatabase

  init {
    mDatabase = mDbHelper.readableDatabase
  }

  fun isOpen(): Boolean {
    return mDatabase.isOpen
  }

  fun getAll(): Cursor {
    requireNonUi()

    return mDatabase.query(
        CitiesTable.TABLE_ACT,
        CitiesTable.ALL_COLUMNS,
        null,
        null,
        null,
        null,
        null)
  }

  fun loadAllCities(): List<City> {
    requireNonUi()

    val cursor: Cursor = mDatabase.query(
        CitiesTable.TABLE_ACT, CitiesTable.ALL_COLUMNS,
        null, null, null, null, null)
    cursor.moveToFirst()

    val cities = ArrayList<City>(cursor.count)
    while (cursor.moveToNext()) {
      cities.add(cursorToCity(cursor))
    }

    // make sure to close the cursor
    cursor.close()
    return cities
  }

  fun cursorToMapPoint(cursor: Cursor): MapPoint {
    requireNonUi()

    val ltd = cursor.getDouble(2).toFloat()
    val lng = cursor.getDouble(3).toFloat()
    val rectangle = Rectangle(ltd, lng, ltd, lng)
    return MapPoint(rectangle, cursor.getInt(0))
  }

  fun getCitiesThatNameContain(name: String): ArrayList<City> {
    requireNonUi()

    val preparedName = "%${name.trim().toLowerCase()}%"
    val cursor = mDatabase.rawQuery(CitiesTable.SELECT_NAME, arrayOf(preparedName))
    cursor.moveToFirst()
    val cities: ArrayList<City> = ArrayList(cursor.count)

    while (!cursor.isAfterLast) {
      cities.add(cursorToCity(cursor))
      cursor.moveToNext()
    }

    cursor.close()
    return cities
  }

  fun getCityById(id: Int): City? {
    requireNonUi()
    var city: City? = null
    val c: Cursor = mDatabase.rawQuery(CitiesTable.SELECT_ID, arrayOf(id.toString()))
    if (c.moveToFirst()) {
      city = cursorToCity(c)
    }

    c.close()
    return city
  }

  private fun cursorToCity(cursor: Cursor): City {
    cursor.let {
      val id = it.getInt(0)
      val name = it.getString(1)
      val country = it.getString(5)
      val timezone = it.getString(4)
      val gmtOffset = it.getFloat(6)
      val latitude = it.getDouble(2)
      val longitude = it.getDouble(3)

      return City(id, name, country, timezone, gmtOffset, latitude, longitude)
    }
  }
}