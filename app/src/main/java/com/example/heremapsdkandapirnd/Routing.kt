package com.example.heremapsdkandapirnd

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.example.heremapsdkandapirnd.models.Waypoints
import com.here.sdk.core.Color
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Point2D
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapView
import com.here.sdk.routing.*
import java.util.*
import kotlin.collections.ArrayList


class Routing(var mapView: MapView, var context: Context) {

    var routingEngine = RoutingEngine()
    private val pinViews = ArrayList<MapView.ViewPin>()

    private val mapMarkerList = ArrayList<MapMarker>()
    private val mapPolylines = ArrayList<MapPolyline>()
    private var startGeoCoordinates: GeoCoordinates? = null
    private var destinationGeoCoordinates: GeoCoordinates? = null
    val routeWayColorList = arrayListOf<Color>(
        Color.valueOf(0f, 0.56f, 0.54f, 0.63f),
        Color.valueOf(0f, 3f, 3f, 3f),
        Color.valueOf(0f, 0f, 0f, 0f),
        Color.valueOf(0f, 0.3f, 0.92f, 0.84f)
    )


    fun addRoute(startLocation: GeoCoordinates, destinationLocation: GeoCoordinates) {
        clearMap()
        startGeoCoordinates = startLocation
        destinationGeoCoordinates = destinationLocation

        val startWaypoint = Waypoint(startGeoCoordinates!!)
        val destinationWaypoint = Waypoint(destinationGeoCoordinates!!)

        val waypoints =
            ArrayList(listOf(startWaypoint, destinationWaypoint))
        val carOptions = CarOptions()
        carOptions.routeOptions.alternatives = 3

        routingEngine.calculateRoute(
            waypoints,
            carOptions
        ) { routingError, routesList ->
            if (routingError == null) {
                routesList!!.forEachIndexed { index, route ->
                    showRouteDetails(route)
                    showRouteOnMap(route, routeWayColorList[index])
                    logRouteViolations(route)
                }
//                val route = routesList!![0]
//                showRouteDetails(route)
//                showRouteOnMap(route)
//                logRouteViolations(route)
            } else {
                showDialog("Error while calculating a route:", routingError.toString())
            }
        }
    }


    fun addRouteWithMultipleDestination(waypointList: List<Waypoints>) {
        startGeoCoordinates = GeoCoordinates(waypointList.first().lat, waypointList.first().lng)
        destinationGeoCoordinates = GeoCoordinates(waypointList.last().lat, waypointList.last().lng)

        val waypoints = ArrayList(waypointList)
        waypoints.removeFirst()
        waypoints.removeLast()

        clearWaypointMapMarker()
        clearRoute()

        val wayPointArrayListDot = ArrayList<Waypoint>()
        wayPointArrayListDot.add(Waypoint(startGeoCoordinates!!))
        waypoints.forEach {
            wayPointArrayListDot.add(Waypoint(GeoCoordinates(it.lat, it.lng)))
        }
        wayPointArrayListDot.add(Waypoint(destinationGeoCoordinates!!))


        routingEngine.calculateRoute(
            wayPointArrayListDot,
            CarOptions(),
        ) { routingError, routesList ->
            if (routingError == null) {
                val route = routesList!![0]
                showRouteDetails(route)
                showRouteOnMap(route, routeWayColorList[0])
                logRouteViolations(route)


                // Draw a circle to indicate the location of the waypoints.
                waypoints.forEach {
                    val textView = TextView(context)
                    textView.setTextColor(android.graphics.Color.WHITE)
                    textView.text = (it.sequence).toString()

                    val linearLayout = LinearLayout(context)
                    linearLayout.setBackgroundResource(R.color.teal_200)
                    linearLayout.setPadding(10, 10, 10, 10)
                    linearLayout.addView(textView)
                    val viewPin = mapView.pinView(linearLayout, GeoCoordinates(it.lat, it.lng))
                    pinViews.add(viewPin!!)
                    //addCircleMapMarker(GeoCoordinates(it.lat,it.lng), R.drawable.red_dot)
                }
            } else {
                showDialog("Error while calculating a route:", routingError.toString())
            }
        }
    }


    fun clearMap() {
        clearWaypointMapMarker()
        clearRoute()
    }

    private fun clearWaypointMapMarker() {
        for (mapMarker in mapMarkerList) {
            mapView.mapScene.removeMapMarker(mapMarker)
        }
        mapMarkerList.clear()

        for (mapViewPin in pinViews) {
            mapViewPin.unpin()
        }
        pinViews.clear()

    }

    private fun clearRoute() {
        for (mapPolyline in mapPolylines) {
            mapView.mapScene.removeMapPolyline(mapPolyline)
        }
        mapPolylines.clear()
    }

    private fun createRandomGeoCoordinatesAroundMapCenter(): GeoCoordinates {
        val centerGeoCoordinates: GeoCoordinates = mapView.viewToGeoCoordinates(
            Point2D((mapView.width / 2).toDouble(), (mapView.height / 2).toDouble())
        )
            ?: // Should never happen for center coordinates.
            throw RuntimeException("CenterGeoCoordinates are null")
        val lat = centerGeoCoordinates.latitude
        val lon = centerGeoCoordinates.longitude
        return GeoCoordinates(
            getRandom(lat - 0.02, lat + 0.02),
            getRandom(lon - 0.02, lon + 0.02)
        )
    }

    private fun getRandom(min: Double, max: Double): Double {
        return min + Math.random() * (max - min)
    }

    private fun showRouteDetails(route: Route) {
        val estimatedTravelTimeInSeconds = route.durationInSeconds.toLong()
        val lengthInMeters = route.lengthInMeters
        val routeDetails = ("Travel Time: " + formatTime(estimatedTravelTimeInSeconds)
                + ", Length: " + formatLength(lengthInMeters))
        showDialog("Route Details", routeDetails)
    }

    private fun showRouteOnMap(route: Route, color: Color) {
        var routeGeoPolyline: GeoPolyline? = null

        try {
            routeGeoPolyline = GeoPolyline(route.polyline)
        } catch (e: InstantiationErrorException) {
            // It should never happen that a route polyline contains less than two vertices.
            return
        }

        val widthInPixels = 20f
        val routeMapPolyline = MapPolyline(
            routeGeoPolyline,
            widthInPixels.toDouble(),
            color
            //Color.valueOf(0f, 0.56f, 0.54f, 0.63f)
        ) // RGBA

        mapView.mapScene.addMapPolyline(routeMapPolyline)
        mapPolylines.add(routeMapPolyline)

        // Draw a circle to indicate starting point and destination.
        addMapMarker(startGeoCoordinates!!, R.drawable.truck2)
        addMapMarker(destinationGeoCoordinates!!, R.drawable.finish)


        // Log maneuver instructions per route section.
        val sections = route.sections
        for (section in sections) {
            logManeuverInstructions(section)
        }

    }

    private fun logManeuverInstructions(section: Section) {
        Log.d("Routing", "Log maneuver instructions per route section:")
        val maneuverInstructions = section.maneuvers
        for (maneuverInstruction in maneuverInstructions) {
            val maneuverAction = maneuverInstruction.action
            val maneuverLocation = maneuverInstruction.coordinates
            val maneuverInfo = (maneuverInstruction.text
                    + ", Action: " + maneuverAction.name
                    + ", Location: " + maneuverLocation.toString())
            Log.d("Routing", maneuverInfo)
        }
    }

    private fun addMapMarker(geoCoordinates: GeoCoordinates, resourceId: Int) {
        val mapImage = MapImageFactory.fromResource(context.resources, resourceId)
        val mapMarker = MapMarker(geoCoordinates, mapImage)
        mapView.mapScene.addMapMarker(mapMarker)
        mapMarkerList.add(mapMarker)

    }


    // A route may contain several warnings, for example, when a certain route option could not be fulfilled.
    // An implementation may decide to reject a route if one or more violations are detected.
    private fun logRouteViolations(route: Route) {
        for (section in route.sections) {
            for (notice in section.notices) {
                Log.e(
                    "Routing",
                    "This route contains the following warning: " + notice.code.toString()
                )
            }
        }
    }

    private fun formatLength(meters: Int): String? {
        val kilometers = meters / 1000
        val remainingMeters = meters % 1000
        return String.format(Locale.getDefault(), "%02d.%02d km", kilometers, remainingMeters)
    }

    private fun formatTime(sec: Long): String? {
        val hours = (sec / 3600).toInt()
        val minutes = (sec % 3600 / 60).toInt()
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }
}