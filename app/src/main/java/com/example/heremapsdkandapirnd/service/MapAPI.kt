package com.example.heremapsdkandapirnd.service

import com.example.heremapsdkandapirnd.models.RouteResponse
import com.example.heremapsdkandapirnd.utils.constants.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface MapAPI {

    @GET("2/findsequence.json")
    suspend fun getRoute(
        @Query("apiKey")
        apiKey:String = API_KEY,
        @Query("start")
        start: String = "",
        @QueryMap map :Map<String,String>,
        @Query("mode")
        mode:String = "fastest;car;traffic:disabled",
    ): Response<RouteResponse>
}