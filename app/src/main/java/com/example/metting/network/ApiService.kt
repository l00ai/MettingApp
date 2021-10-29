package com.example.metting.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface ApiService {

    @POST("send")
    fun sendRemoteMessage(
        @HeaderMap header: HashMap<String, String>,
        @Body remoteBody: String
    ): Call<String>

}