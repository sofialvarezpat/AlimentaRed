package com.example.ecoabastos.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL pública de tu API desplegada en Render (debe terminar en "/")
    private const val BASE_URL = "https://alimentared-bxhk.onrender.com/"

    // Interceptor obligatorio con nivel BODY para auditar todas las peticiones y respuestas HTTP en Logcat
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP_BODY", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Render (plan gratuito) "duerme" el servidor tras inactividad:
    // la primera petición puede tardar 30-60 s en despertar.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
