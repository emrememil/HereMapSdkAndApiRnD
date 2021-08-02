package com.example.heremapsdkandapirnd

import android.app.AlertDialog
import android.content.Context
import com.here.sdk.core.*
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapView
import com.here.sdk.search.*
import com.here.sdk.search.SearchCallback
import java.util.*
import kotlin.collections.ArrayList


class Search(var mapView: MapView, var context: Context) {

    var searchEngine = SearchEngine()

    private val mapMarkerList: ArrayList<MapMarker> = ArrayList()

    var searchEngineListener: SearchEngineListener? = null


    fun setOnClickListener(searchEngineListener: SearchEngineListener) {
        this.searchEngineListener = searchEngineListener
    }

    fun searchExample(searchTerm: String) {
        searchInViewport(searchTerm)
    }

    private fun searchInViewport(queryString: String) {
        //clearMap()
        val viewportGeoBox: GeoBox = getMapViewGeoBox()
        val query = TextQuery(queryString, viewportGeoBox)
        val maxItems = 30
        val searchOptions = SearchOptions(LanguageCode.EN_US, maxItems)

        searchEngine.search(query, searchOptions, querySearchCallback)
    }

    private fun getMapViewGeoBox(): GeoBox {
        val mapViewWidthInPixels = mapView.width
        val mapViewHeightInPixels = mapView.height
        val bottomLeftPoint2D = Point2D(
            (0).toDouble(),
            mapViewHeightInPixels.toDouble()
        )
        val topRightPoint2D = Point2D(
            mapViewWidthInPixels.toDouble(), (0).toDouble()
        )
        val southWestCorner = mapView.viewToGeoCoordinates(bottomLeftPoint2D)
        val northEastCorner = mapView.viewToGeoCoordinates(topRightPoint2D)
        if (southWestCorner == null || northEastCorner == null) {
            throw RuntimeException("GeoBox creation failed, corners are null.")
        }

        // Note: This algorithm assumes an unrotated map view.
        return GeoBox(southWestCorner, northEastCorner)
    }

    private val querySearchCallback =
        SearchCallback { searchError, list ->
            if (searchError != null) {
                //showDialog("Search", "Error: $searchError")
                return@SearchCallback
            }
            for (searchResult in list!!) {
                searchEngineListener?.onLocationFound(list)
            }
        }

    fun addPoiMapMarker(geoCoordinates: GeoCoordinates?, metadata: Metadata) {
        geoCoordinates?.let {
            val mapMarker = createPoiMapMarker(geoCoordinates)
            mapMarker.metadata = metadata
            mapView.mapScene.addMapMarker(mapMarker)
            mapMarkerList.add(mapMarker)
        }
    }

    fun addPoiMapMarker(geoCoordinates: GeoCoordinates) {
        val mapMarker = createPoiMapMarker(geoCoordinates)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)
    }


    private fun createPoiMapMarker(geoCoordinates: GeoCoordinates): MapMarker {
        val mapImage = MapImageFactory.fromResource(context.resources, R.drawable.poi)
        return MapMarker(geoCoordinates, mapImage, Anchor2D(0.5, (1).toDouble()))
    }


    fun clearMap() {
        for (mapMarker in mapMarkerList) {
            mapView.mapScene.removeMapMarker(mapMarker)
        }
        mapMarkerList.clear()
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }

    companion object {
        const val TAG = "Search"
    }
}


interface SearchEngineListener {
    fun onLocationFound(placeList: MutableList<Place>)
}

