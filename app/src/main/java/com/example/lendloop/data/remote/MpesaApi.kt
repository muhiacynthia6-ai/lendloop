package com.example.lendloop.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MpesaApi {

    @GET("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String
    ): TokenResponse

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiateStkPush(
        @Header("Authorization") token: String,
        @Body request: StkPushRequest
    ): StkPushResponse
}
