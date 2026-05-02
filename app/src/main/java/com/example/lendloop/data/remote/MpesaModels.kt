package com.example.lendloop.data.remote

import com.google.gson.annotations.SerializedName

// ─── Token ───────────────────────────────────────────────
data class TokenResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("expires_in")   val expires_in: String
)

// ─── STK Push Request ────────────────────────────────────
data class StkPushRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String,
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

// ─── STK Push Response ───────────────────────────────────
data class StkPushResponse(
    @SerializedName("MerchantRequestID")    val MerchantRequestID: String?,
    @SerializedName("CheckoutRequestID")    val CheckoutRequestID: String?,
    @SerializedName("ResponseCode")         val ResponseCode: String?,
    @SerializedName("ResponseDescription")  val ResponseDescription: String?,
    @SerializedName("CustomerMessage")      val CustomerMessage: String?
)