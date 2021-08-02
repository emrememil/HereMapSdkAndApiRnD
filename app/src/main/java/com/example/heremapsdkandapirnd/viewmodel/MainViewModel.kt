package com.example.heremapsdkandapirnd.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heremapsdkandapirnd.models.RouteResponse
import com.example.heremapsdkandapirnd.models.Waypoints
import com.example.heremapsdkandapirnd.repository.RouteRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(
    var routeRepository: RouteRepository
) : ViewModel() {

    val waypoints = MutableLiveData<List<Waypoints>>()
    val error = MutableLiveData<Boolean>()

    fun getRoute(
        start: String,
        geoCoordinates: HashMap<String,String>
    ) = viewModelScope.launch {
        val response = handleRouteResponse(
            routeRepository.getRoute(
                start,
                geoCoordinates
            )
        )

        if (response != null) {
            waypoints.value = response.results[0].waypoints
        } else {
            error.value = true
        }
    }



    private fun handleRouteResponse(response: Response<RouteResponse>): RouteResponse? {
        if (response.isSuccessful) {
            response.body()?.let {
                return it
            }
            return null
        } else return null
    }
}