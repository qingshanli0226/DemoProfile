package com.example.bottombar.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitCreator {

    companion object {
        private var apiService:ApiService? = null
        private lateinit var retrofit: Retrofit
        private var baseUrl = "http://169.254.230.253:8080/"
        fun getApiService():ApiService {
            if (apiService == null) {
                var okhttpClient = OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.SECONDS)
                    .build()
                retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(baseUrl)
                    .client(okhttpClient)
                    .build()
                apiService = retrofit.create(ApiService::class.java)
            }

            return apiService!!
        }
    }



}