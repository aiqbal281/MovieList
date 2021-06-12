package com.adil.movielist.baseClass

import com.adil.movielist.retrofit.ApiException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class BaseRepository {

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody()?.string()

            val message = StringBuilder()
            error?.let {
                try {
                    message.append(JSONObject(it).getString("Error"))
                } catch (e: JSONException) {
                }
            }
            throw ApiException(message.toString())
        }
    }


//    suspend fun <T> safeApiCall(
//        apiCall: suspend () -> T
//    ): Resource<T> {
//        return withContext(Dispatchers.Default) {
//            try {
//                Resource.Success(apiCall.invoke())
//            } catch (throwable: Throwable) {
//                when (throwable) {
//                    is HttpException -> {
//                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
//                    }
//                    else -> {
//                        Resource.Failure(true, null, null)
//                    }
//                }
//            }
//        }
//    }

}