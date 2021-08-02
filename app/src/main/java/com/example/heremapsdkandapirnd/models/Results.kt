package com.example.heremapsdkandapirnd.models

import com.google.gson.annotations.SerializedName


data class Results (

	@SerializedName("waypoints") val waypoints : List<Waypoints>,
	@SerializedName("distance") val distance : Int,
	@SerializedName("time") val time : Int,
	@SerializedName("interconnections") val interconnections : List<Interconnections>,
	@SerializedName("description") val description : String,
	@SerializedName("timeBreakdown") val timeBreakdown : TimeBreakdown
)