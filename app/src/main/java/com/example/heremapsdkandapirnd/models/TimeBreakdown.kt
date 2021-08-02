package com.example.heremapsdkandapirnd.models

import com.google.gson.annotations.SerializedName



data class TimeBreakdown (

	@SerializedName("driving") val driving : Int,
	@SerializedName("service") val service : Int,
	@SerializedName("rest") val rest : Int,
	@SerializedName("waiting") val waiting : Int
)