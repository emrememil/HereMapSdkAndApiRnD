package com.example.heremapsdkandapirnd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.heremapsdkandapirnd.adapters.SearchLocationAdapter
import com.example.heremapsdkandapirnd.dialog.RouteInfo
import com.example.heremapsdkandapirnd.dialog.RoutesDialog
import com.example.heremapsdkandapirnd.dialog.RoutesDialogListener
import com.example.heremapsdkandapirnd.repository.RouteRepository
import com.example.heremapsdkandapirnd.utils.PermissionUtils
import com.example.heremapsdkandapirnd.viewmodel.MainViewModel
import com.example.heremapsdkandapirnd.viewmodel.MainViewModelProviderFactory
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.*
import com.here.sdk.search.Place
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), RoutesDialogListener {

    lateinit var viewModel: MainViewModel
    private lateinit var mapView: MapView
    lateinit var routing: Routing
    lateinit var search: Search
    lateinit var myGeoCoordinates: GeoCoordinates

    var searchLocationAdapter = SearchLocationAdapter()

    var startLocation: GeoCoordinates? = null
    var destinationPlaces = ArrayList<Place>()
    private var geoCoordinatesToCalculate = HashMap<String, String>()
    private var locationIndicator = LocationIndicator()

    lateinit var positioningProvider: PositioningProvider


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val routeRepository = RouteRepository()
        val viewModelProviderFactory = MainViewModelProviderFactory(routeRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(MainViewModel::class.java)

        mapView = map_view
        mapView.onCreate(savedInstanceState)

        positioningProvider = PositioningProvider(this)


        checkPermissions()


        routing = Routing(mapView, this)
        search = Search(mapView, this)

        locationRv.apply {
            adapter = searchLocationAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        search.setOnClickListener(object : SearchEngineListener {
            override fun onLocationFound(placeList: MutableList<Place>) {
                searchLocationAdapter.setItem(placeList)
            }
        })

        searchLocationAdapter.setOnItemClickListener { place ->
            destinationPlaces.add(place)

            etDestination.text.clear()
            etDestination.hideKeyboard()
            search.addPoiMapMarker(place.geoCoordinates!!)
            locationRv.visibility = View.GONE
        }

        etDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    search.searchExample(s.toString())
                    locationRv.visibility = View.VISIBLE
                } else {
                    locationRv.visibility = View.GONE
                }
            }
        })

        btnRoutes.setOnClickListener {
            val routeInfo = RouteInfo(startLocation!!, destinationPlaces, routing, search)
            RoutesDialog.newInstance(routeInfo).show(
                supportFragmentManager,
                RoutesDialog.TAG
            )
        }

        btnClearMap.setOnClickListener {
            routing.clearMap()
            search.clearMap()
        }

        viewModel.error.observe(this, {
            if (it) {
                Toast.makeText(this, "error ocurred when getting route", Toast.LENGTH_LONG).show()
            }
        })

        viewModel.waypoints.observe(this, {
            routing.addRouteWithMultipleDestination(it)
            removeLocationIndicator()
        })

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (PermissionUtils.hasPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        ) {
            //loadMapScene()
            locating()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun locating() {
        positioningProvider.startLocating(object :
            PositioningProvider.PlatformLocationListener {
            override fun onLocationUpdated(location: Location?) {
                Log.e("Location Latitude: ", location?.latitude.toString())
                Log.e("Location Longitude: ", location?.longitude.toString())
                location?.let {
                    loadMapScene(location)
                }

            }

        })
    }

    private fun loadMapScene() {
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY, object : MapScene.LoadSceneCallback {
            override fun onLoadScene(mapError: MapError?) {
                if (mapError == null) {
                    val distanceInMeters: Double = (2000).toDouble()
                    myGeoCoordinates = GeoCoordinates(39.9080, 32.8092)
                    startLocation = myGeoCoordinates

                    mapView.camera.lookAt(
                        myGeoCoordinates,
                        distanceInMeters
                    )
                    addLocationIndicator(
                        myGeoCoordinates,
                        LocationIndicator.IndicatorStyle.NAVIGATION
                    )

                    mapView.mapScene.setLayerState(
                        MapScene.Layers.TRAFFIC_FLOW,
                        MapScene.LayerState.VISIBLE
                    )
                    mapView.mapScene.setLayerState(
                        MapScene.Layers.TRAFFIC_INCIDENTS,
                        MapScene.LayerState.VISIBLE
                    )
                } else {
                    Log.e("TAG", "Loading map failed: mapError: " + mapError.name);
                }
            }

        })
    }

    private fun loadMapScene(location: Location) {
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY, object : MapScene.LoadSceneCallback {
            override fun onLoadScene(mapError: MapError?) {
                if (mapError == null) {
                    val distanceInMeters: Double = (2000).toDouble()
                    myGeoCoordinates = GeoCoordinates(location.latitude, location.longitude)
                    startLocation = myGeoCoordinates

                    mapView.camera.lookAt(
                        myGeoCoordinates,
                        distanceInMeters
                    )
                    addLocationIndicator(
                        myGeoCoordinates,
                        LocationIndicator.IndicatorStyle.NAVIGATION
                    )

                    mapView.mapScene.setLayerState(
                        MapScene.Layers.TRAFFIC_FLOW,
                        MapScene.LayerState.VISIBLE
                    )
                    mapView.mapScene.setLayerState(
                        MapScene.Layers.TRAFFIC_INCIDENTS,
                        MapScene.LayerState.VISIBLE
                    )
                } else {
                    Log.e("TAG", "Loading map failed: mapError: " + mapError.name);
                }
            }

        })
    }

    private fun addLocationIndicator(
        geoCoordinates: GeoCoordinates,
        indicatorStyle: LocationIndicator.IndicatorStyle
    ) {
        locationIndicator.locationIndicatorStyle = indicatorStyle

        val location = com.here.sdk.core.Location.Builder()
            .setCoordinates(geoCoordinates)
            .setTimestamp(Date())
            .setBearingInDegrees((360).toDouble())
            .build();
        locationIndicator.updateLocation(location)

        mapView.addLifecycleListener(locationIndicator)
    }

    private fun removeLocationIndicator() {
        mapView.removeLifecycleListener(locationIndicator)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //loadMapScene()
                locating()
            } else {
                Toast.makeText(
                    this,
                    "permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    companion object {
        const val TAG = "TAGMainActivity"
        const val LOCATION_PERMISSION_REQUEST = 100
    }

    override fun onClickedCreateRoute() {
        destinationPlaces.forEachIndexed { index, place ->
            geoCoordinatesToCalculate.put(
                "destination${index + 1}",
                "des${index + 1}" + ";" + place.geoCoordinates!!.latitude + "," + place.geoCoordinates!!.longitude
            )
        }

        val startString = "Start;" + startLocation!!.latitude + "," + startLocation!!.longitude
        viewModel.getRoute(startString, geoCoordinatesToCalculate)
    }

}