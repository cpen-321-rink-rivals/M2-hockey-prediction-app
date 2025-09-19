package com.cpen321.usermanagement.data.remote.api;



import com.cpen321.usermanagement.data.remote.dto.ApiResponse;
import com.cpen321.usermanagement.data.remote.dto.LanguagesData;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;

interface LanguageInterface {
    @GET("languages_spoken")
    suspend fun getAvailableLanguages(@Header("Authorization")authHeader: String): Response<ApiResponse<LanguagesData>>
}
