package com.cpen321.usermanagement.data.remote.dto

import android.os.Message

data class Network(
    val company: List<String>,
    val href: String,
    val location: Location, // A nested data class
    val name: String,
    val id: String
)

data class Location(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class BicycleDataModel(
    val networks: List<Network>
)