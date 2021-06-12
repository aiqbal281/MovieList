package com.adil.movielist.retrofit


import androidx.viewbinding.BuildConfig
import com.adil.movielist.common.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitClient {

    fun <Api> getAPI(api: Class<Api>): Api {
        return Retrofit.Builder()
            .baseUrl(Constant.baseUrl)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(chain.request().newBuilder().also {
                            it.addHeader("Authorization", "Bearer ")
                        }.build())
                    }
                    .addInterceptor(NetworkLogger()).also { client ->
                        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
                            val logging = HttpLoggingInterceptor()
                            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                            client.addInterceptor(logging)
                        }
                    }.build()

            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

}