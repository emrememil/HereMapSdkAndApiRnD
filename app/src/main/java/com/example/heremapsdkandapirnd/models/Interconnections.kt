package com.example.heremapsdkandapirnd.models

import com.google.gson.annotations.SerializedName


data class Interconnections (
	@SerializedName("fromWaypoint") val fromWaypoint : String,
	@SerializedName("toWaypoint") val toWaypoint : String,
	@SerializedName("distance") val distance : Int,
	@SerializedName("time") val time : Int,
	@SerializedName("rest") val rest : Int,
	@SerializedName("waiting") val waiting : Int
)