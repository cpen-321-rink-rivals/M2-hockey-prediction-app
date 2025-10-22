package com.cpen321.usermanagement.data.remote.api

import retrofit2.http.GET

interface NHLInterface {

    @GET("/v1/skater-stats-leaders/current")
    suspend fun 
}