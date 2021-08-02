package com.example.heremapsdkandapirnd.models

import com.google.gson.annotations.SerializedName


data class RouteResponse (

    @SerializedName("results") val results : List<Results>,
    @SerializedName("errors") val errors : List<String>,
    @SerializedName("processingTimeDesc") val processingTimeDesc : String,
    @SerializedName("responseCode") val responseCode : Int,
    @SerializedName("warnings") val warnings : String,
    @SerializedName("requestId") val requestId : String
)