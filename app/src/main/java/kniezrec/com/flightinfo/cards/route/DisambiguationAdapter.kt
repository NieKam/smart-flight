package kniezrec.com.flightinfo.cards.route

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.db.City

/**
 * Copyright by Kamil Niezrecki
 */
class DisambiguationAdapter(private val mListener: ClickListener) :
    RecyclerView.Adapter<DisambiguationAdapter.DisambiguationViewHolder>() {

  interface ClickListener {
    fun onCityClicked(city: City)
  }

  private var mCities: List<City> = listOf()

  fun addAll(cities: List<City>) {
    mCities = cities
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisambiguationViewHolder {
    val itemView = LayoutInflater.from(parent.context)
        .inflate(R.layout.disambiguation_item, parent, false) as TextView

    return DisambiguationViewHolder(itemView)
  }

  override fun getItemCount() = mCities.size

  override fun onBindViewHolder(holder: DisambiguationViewHolder, position: Int) {
    val city = mCities[position]
    holder.apply {
      setCity(city, position)
      cityLabel.setOnClickListener { mListener.onCityClicked(city) }
    }
  }

  inner class DisambiguationViewHolder(val cityLabel: TextView) :
      RecyclerView.ViewHolder(cityLabel) {

    @SuppressLint("SetTextI18n")
    fun setCity(city: City, position: Int) {
      cityLabel.text = "${position + 1}. ${city.name} (${city.country})"
    }
  }
}