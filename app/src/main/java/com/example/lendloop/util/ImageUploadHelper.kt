package com.example.lendloop.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun uploadImageToServer(
    uri: Uri,
    context: Context,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Could not read image")

            val bytes       = inputStream.readBytes()
            val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "upload_${System.currentTimeMillis()}.jpg", requestBody)
                .build()

            val request = Request.Builder()
                .url("https://yourapi.com/upload")
                .post(multipart)
                .build()

            val response = OkHttpClient().newCall(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Image uploaded ✅", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    val msg = "Upload failed: ${response.code}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    onError(msg)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val msg = "Error: ${e.message}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                onError(msg)
            }
        }
    }
}