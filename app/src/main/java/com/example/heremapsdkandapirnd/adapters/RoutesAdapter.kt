package com.example.heremapsdkandapirnd.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.heremapsdkandapirnd.R
import com.here.sdk.search.Place
import kotlinx.android.synthetic.main.item_route.view.*

class RoutesAdapter : RecyclerView.Adapter<RoutesAdapter.RoutesViewHolder>() {

    var locations = ArrayList<Place>()

    inner class RoutesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        return RoutesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_route, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        val location = locations[position]

        holder.itemView.apply {
            tvNumber.text = (position + 1).toString()
            tvAddress.text = location.address.addressText
            tvDetailsAddress.text = location.address.city + "," + location.address.country
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    private var onItemClickListener: ((Place) -> Unit)? = null

    fun setOnItemClickListener(listener: (Place) -> Unit) {
        onItemClickListener = listener
    }

    fun setItem(placesList: List<Place>) {
        this.locations = java.util.ArrayList(placesList)
        notifyDataSetChanged()
    }
}