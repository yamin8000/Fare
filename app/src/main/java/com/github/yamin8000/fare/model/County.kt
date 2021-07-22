package com.github.yamin8000.fare.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class County(val id : Int, val name : String, @Json(name = "state_id") val stateId : String)