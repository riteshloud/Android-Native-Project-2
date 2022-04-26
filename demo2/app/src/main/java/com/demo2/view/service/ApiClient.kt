package com.demo2.view.service

import android.app.Activity
import com.demo2.utilities.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit

object ApiClient {
     var BASE_URL = "localhost:/v2"  //Temp dev2
    private var retrofit: Retrofit? = null
    private var toastLessRetrofit: Retrofit? = null

    val getToastLessClient: ApiInterface
        get() {
            val client = OkHttpClient.Builder()
                .connectTimeout(Constants.connectionTimeOut, TimeUnit.SECONDS)
                .readTimeout(Constants.readTimeOut, TimeUnit.SECONDS).build()
            if (toastLessRetrofit == null) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                val httpClient = OkHttpClient.Builder()
                httpClient.addInterceptor(logging)
                toastLessRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            }
            return toastLessRetrofit!!.create(ApiInterface::class.java)
        }

    fun getClient(context: Activity): ApiInterface {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .connectTimeout(Constants.connectionTimeOut, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(ConnectivityInterceptor(context))
            .readTimeout(Constants.readTimeOut, TimeUnit.SECONDS).build()

        if (retrofit == null) {
            // <-- this is the important line!
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        return retrofit!!.create(ApiInterface::class.java)

    }



}
