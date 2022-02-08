package kniezrec.com.flightinfo.db

import android.os.Parcel
import android.os.Parcelable

/**
 * Copyright by Kamil Niezrecki
 */
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val timezone: String,
    val gmtOffset: Float,
    val latitude: Double,
    val longitude: Double
) : Parcelable {

  constructor(parcel: Parcel) :
      this(
          parcel.readInt(),
          requireNotNull(parcel.readString()),
          requireNotNull(parcel.readString()),
          requireNotNull(parcel.readString()),
          parcel.readFloat(),
          parcel.readDouble(),
          parcel.readDouble()
      )

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeInt(id)
    dest.writeString(name)
    dest.writeString(country)
    dest.writeString(timezone)
    dest.writeFloat(gmtOffset)
    dest.writeDouble(latitude)
    dest.writeDouble(longitude)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<City> {
    override fun createFromParcel(parcel: Parcel): City {
      return City(parcel)
    }

    override fun newArray(size: Int): Array<City?> {
      return arrayOfNulls(size)
    }
  }
}