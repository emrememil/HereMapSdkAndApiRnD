package com.example.heremapsdkandapirnd.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.heremapsdkandapirnd.R
import com.here.sdk.search.Place
import kotlinx.android.synthetic.main.search_location_item.view.*

class SearchLocationAdapter : RecyclerView.Adapter<SearchLocationAdapter.LocationViewHolder>() {

    var locations = ArrayList<Place>()

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LocationViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.search_location_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]

        holder.itemView.apply {
            tvAddress.text = location.address.addressText
            tvDetailsAddress.text = location.address.city + "," + location.address.country
            setOnClickListener {
                onItemClickListener?.let { it(location) }
            }
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    private var onItemClickListener: ((Place) -> Unit)? = null

    fun setOnItemClickListener(listener: (Place) -> Unit) {
        onItemClickListener = listener
    }

    fun setItem(placesList: MutableList<Place>) {
        this.locations = java.util.ArrayList(placesList)
        notifyDataSetChanged()
    }

}