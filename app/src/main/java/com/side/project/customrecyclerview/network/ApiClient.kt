package com.side.project.customrecyclerview.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    /**
     * HTTP 攔截器
     */
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(LogInterceptor())
        .build()

    /**
     * API Server
     */
    val getAPI: ApiService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://www.flickr.com/services/feeds/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}