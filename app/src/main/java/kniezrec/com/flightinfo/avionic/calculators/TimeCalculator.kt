package kniezrec.com.flightinfo.avionic.calculators

import android.text.format.DateUtils
import kniezrec.com.flightinfo.db.City
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*

/**
 * Copyright by Kamil Niezrecki
 */
class TimeCalculator {

  private val mDecimalFormat: DecimalFormat = DecimalFormat("+#,##0.00;-#")

  init {
    mDecimalFormat.positivePrefix = "+"
    mDecimalFormat.negativePrefix = "-"
    val dfs = mDecimalFormat.decimalFormatSymbols
    dfs.decimalSeparator = ':'
    mDecimalFormat.decimalFormatSymbols = dfs
  }

  internal fun getTimeInCity(city: City, timeInMs: Long, includeOffset: Boolean): String {
    var newGmtOffset: Long = city.gmtOffset.toLong()
    val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
    val tmzID = TimeZone.getTimeZone(city.timezone)

    dateFormat.timeZone = tmzID
    val offset = tmzID.dstSavings
    if (offset != 0) {
      newGmtOffset += (offset / DateUtils.HOUR_IN_MILLIS)
    }

    val correctedDate = Date(timeInMs + newGmtOffset)
    val formattedTime = dateFormat.format(correctedDate)

    return if (includeOffset) {
      "$formattedTime (${mDecimalFormat.format(newGmtOffset)})"
    } else {
      formattedTime
    }
  }

  fun getTimeInCity(city: City): String {
    return getTimeInCity(city, System.currentTimeMillis(), true)
  }

  internal fun getEstimatedArrivalTime(city: City, timeInMs: Long, remainingTimeInSec: Long): Any {
      val calendar = Calendar.getInstance().apply {
          timeInMillis = timeInMs
          add(Calendar.SECOND, remainingTimeInSec.toInt())
      }
    return getTimeInCity(city, calendar.timeInMillis, false)
  }

  fun getEstimatedArrivalTime(city: City, remainingTimeInSec: Long): Any {
    return getEstimatedArrivalTime(city, System.currentTimeMillis(), remainingTimeInSec)
  }
}