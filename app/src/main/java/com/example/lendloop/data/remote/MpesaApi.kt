package com.example.lendloop.data.remote

import retrofit2.http.*

data class MpesaTokenResponse(
    val access_token: String,
    val expires_in: String
)

data class StkPushRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String = "CustomerPayBillOnline",
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

data class StkPushResponse(
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResponseCode: String?,
    val ResponseDescription: String?,
    val CustomerMessage: String?
)

interface MpesaApi {

    @GET("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String
    ): MpesaTokenResponse

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiateStkPush(
        @Header("Authorization") authorization: String,
        @Body request: StkPushRequest
    ): StkPushResponse
}