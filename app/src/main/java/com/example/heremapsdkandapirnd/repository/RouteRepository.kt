package com.example.heremapsdkandapirnd.repository

import com.example.heremapsdkandapirnd.service.ServiceManager

class RouteRepository {
    suspend fun getRoute(
        start: String,
        geoCoordinates: HashMap<String,String>
    ) =
        ServiceManager.api.getRoute(
            start = start,
            map = geoCoordinates
        )

}