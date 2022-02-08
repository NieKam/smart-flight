package kniezrec.com.flightinfo.db

import android.database.sqlite.SQLiteDatabase
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */

object CitiesTable {

  private const val COLUMN_ID = "_id"

  const val TABLE_ACT = "cities_info"
  private const val COL_CITY_NAME = "city"
  private const val COL_LAT = "latitude"
  private const val COL_LNG = "longitude"
  private const val COL_TIMEZONE = "timezone"
  private const val COL_COUNTRY = "country"
  private const val COL_GMT = "gmt_offset"

  val ALL_COLUMNS = arrayOf(
      COLUMN_ID,
      COL_CITY_NAME,
      COL_LAT,
      COL_LNG,
      COL_TIMEZONE,
      COL_COUNTRY,
      COL_GMT)

  private const val DATABASE_CREATE =
      "create table $TABLE_ACT" +
          "($COLUMN_ID integer primary key autoincrement," +
          "$COL_CITY_NAME text not null," +
          "$COL_LAT real not null," +
          "$COL_LNG real not null," +
          "$COL_TIMEZONE text not null," +
          "$COL_COUNTRY text not null," +
          "$COL_GMT real not null);"

  const val SELECT_ID = "SELECT * FROM $TABLE_ACT WHERE $COLUMN_ID=?"
  const val SELECT_NAME = "SELECT * FROM $TABLE_ACT WHERE LOWER($COL_CITY_NAME) LIKE LOWER(?)"

  @JvmStatic
  private fun onCreate(database: SQLiteDatabase) {
    Timber.d("Execute onCreate: $DATABASE_CREATE")
    database.execSQL(DATABASE_CREATE)
  }

  @JvmStatic
  fun onUpgrade(database: SQLiteDatabase) {
    Timber.d("Execute onUpgrade")
    database.execSQL("DROP TABLE IF EXISTS $TABLE_ACT")
    onCreate(database)
  }
}
