package com.github.yamin8000.fare2.model

import com.google.gson.annotations.SerializedName

data class County(
    val id: Int,
    val name: String,
    @SerializedName("state_id")
    val stateId: String
)