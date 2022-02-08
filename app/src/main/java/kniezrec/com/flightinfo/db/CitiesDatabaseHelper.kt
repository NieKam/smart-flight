package kniezrec.com.flightinfo.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

/**
 * Copyright by Kamil Niezrecki
 */

class CitiesDatabaseHelper(context: Context) :
    SQLiteAssetHelper(context, "cities_info.db", null, 2) {

  override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    CitiesTable.onUpgrade(database)
  }
}