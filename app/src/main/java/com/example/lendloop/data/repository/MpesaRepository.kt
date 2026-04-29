package com.example.lendloop.data.repository

import com.example.lendloop.data.remote.MpesaApi
import com.example.lendloop.data.remote.MpesaConfig
import com.example.lendloop.data.remote.StkPushRequest
import com.example.lendloop.data.remote.StkPushResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            .build()

        Retrofit.Builder()
            .baseUrl(MpesaConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(MpesaApi::class.java)
    }

    suspend fun initiateStkPush(
        phoneNumber: String,
        amount: Double,
        accountRef: String
    ): Result<StkPushResponse> {
        return try {
            // Get access token
            val token = api.getAccessToken(MpesaConfig.getBasicAuth())
            val bearer = "Bearer ${token.access_token}"

            // Format phone number — convert 07xx to 2547xx
            val formattedPhone = formatPhone(phoneNumber)

            val timestamp = MpesaConfig.getTimestamp()
            val password = MpesaConfig.getPassword(timestamp)

            val request = StkPushRequest(
                BusinessShortCode = MpesaConfig.SHORTCODE,
                Password = password,
                Timestamp = timestamp,
                Amount = amount.toInt().toString(),
                PartyA = formattedPhone,
                PartyB = MpesaConfig.SHORTCODE,
                PhoneNumber = formattedPhone,
                CallBackURL = MpesaConfig.CALLBACK_URL,
                AccountReference = accountRef,
                TransactionDesc = "LendLoop Payment"
            )

            val response = api.initiateStkPush(bearer, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatPhone(phone: String): String {
        val cleaned = phone.replace(" ", "").replace("-", "")
        return when {
            cleaned.startsWith("0") -> "254${cleaned.substring(1)}"
            cleaned.startsWith("+") -> cleaned.substring(1)
            else -> cleaned
        }
    }
}