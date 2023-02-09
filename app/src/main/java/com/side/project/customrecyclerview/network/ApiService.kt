package com.side.project.customrecyclerview.network

import com.side.project.customrecyclerview.data.Image
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("photos_public.gne")
    fun getImage(
        @Query("format") format: String = "json",
        @Query("nojsoncallback") nojsoncallback: Int = 1,
    ) : Call<Image>
}