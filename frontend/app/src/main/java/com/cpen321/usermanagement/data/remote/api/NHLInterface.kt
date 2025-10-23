package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ScheduleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NHLInterface {

    // Current schedule as of now
    @GET("v1/schedule/now")
    suspend fun getCurrentSchedule(): Response<ScheduleResponse>

}