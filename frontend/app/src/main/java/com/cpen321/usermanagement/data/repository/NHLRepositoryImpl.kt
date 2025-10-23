package com.cpen321.usermanagement.data.repository

import android.content.Context
import android.util.Log
import com.cpen321.usermanagement.data.remote.api.NHLInterface
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.GameWeek
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NHLRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nhlInterface: NHLInterface
): NHLRepository {

    companion object {
        private const val TAG = "NHLRepositoryImpl"
    }

    override suspend fun getCurrentSchedule(): Result<List<GameWeek>> {
        return try {
            val response = nhlInterface.getCurrentSchedule()


            Log.d("RAW", response.toString())
            Log.d("response", "${response.body()}")


            if (response.isSuccessful && response.body()?.gameWeek != null) {
                Result.success(response.body()!!.gameWeek)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch NHL Schedule.")
                Log.e(TAG, "Failed to get NHL Schedule: $errorBodyString")


                Result.failure(Exception(errorMessage))
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while getting NHL Schedule", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while getting NHL Schedule", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting NHL Schedule", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while getting NHL Schedule: ${e.code()}", e)
            Result.failure(e)
        }
    }

}
