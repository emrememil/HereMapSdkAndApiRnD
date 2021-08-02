package com.example.heremapsdkandapirnd.models

import com.google.gson.annotations.SerializedName

data class Waypoints (

	@SerializedName("id") val id : String,
	@SerializedName("lat") val lat : Double,
	@SerializedName("lng") val lng : Double,
	@SerializedName("sequence") val sequence : Int,
	@SerializedName("estimatedArrival") val estimatedArrival : String,
	@SerializedName("estimatedDeparture") val estimatedDeparture : String,
	@SerializedName("fulfilledConstraints") val fulfilledConstraints : List<String>
)