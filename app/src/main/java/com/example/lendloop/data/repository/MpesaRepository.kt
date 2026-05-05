package com.example.lendloop.data.repository

import android.util.Log
import com.example.lendloop.data.remote.MpesaApi
import com.example.lendloop.data.remote.MpesaConfig
import com.example.lendloop.data.remote.StkPushRequest
import com.example.lendloop.data.remote.StkPushResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MpesaRepository @Inject constructor() {

    private val api: MpesaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(MpesaConfig.BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(GsonBuilder().setLenient().create())
            )
            .build()
            .create(MpesaApi::class.java)
    }
    suspend fun initiateStkPush(
        phoneNumber: String,
        amount: Double,
        accountRef: String
    ): Result<StkPushResponse> {
        return try {
            val token = getAccessToken()
            val bearer = "Bearer $token"
            Log.d("MPESA", "Token obtained: $token")
            val timestamp = MpesaConfig.getTimestamp()
            val password = generatePassword(timestamp)
            val phone = formatPhone(phoneNumber)

            val request = StkPushRequest(
                BusinessShortCode = MpesaConfig.SHORTCODE,
                Password = password,
                Timestamp = timestamp,
                TransactionType = "CustomerPayBillOnline",
                Amount = amount.toInt().toString(),
                PartyA = phone,
                PartyB = MpesaConfig.SHORTCODE,
                PhoneNumber = phone,
                CallBackURL = MpesaConfig.CALLBACK_URL,
                AccountReference = accountRef,
                TransactionDesc = "LendLoop Payment"
            )

            Log.d("MPESA", "STK push request: $request")
            val response = api.initiateStkPush(bearer, request)
            Log.d("MPESA", "STK push response: $response")

            if (response.ResponseCode == "0") {
                Result.success(response)
            } else {
                Result.failure(
                    Exception(response.ResponseDescription ?: "STK push failed")
                )
            }

        } catch (e: Exception) {
            Log.e("MPESA", "Error: ${e.message}", e)
            Result.failure(Exception("M-Pesa error: ${e.message}"))
        }
    }
    private suspend fun getAccessToken(): String {
        val tokenResponse = api.getAccessToken(MpesaConfig.getBasicAuth())
        return tokenResponse.access_token
    }
    private fun generatePassword(timestamp: String): String {
        val raw = "${MpesaConfig.SHORTCODE}${MpesaConfig.PASSKEY}$timestamp"
        return android.util.Base64.encodeToString(
            raw.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
    }
    private fun formatPhone(phone: String): String {
        val cleaned = phone.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("+", "")
        return when {
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"
            cleaned.startsWith("254") -> cleaned
            else -> "254$cleaned"
        }
    }
}