package com.melondemo.parkingwidget.data

import com.melondemo.parkingwidget.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Create an OkHttp interceptor to add headers (Authorization etc)
val authInterceptor = Interceptor { chain ->
    val originalRequest: Request = chain.request()
    val requestWithHeaders = originalRequest.newBuilder()
        .header("Authorization", "apikey ${BuildConfig.PARKING_API_KEY}")  // Replace with your key dynamically
        .header("Accept", "application/json")
        .build()
    chain.proceed(requestWithHeaders)
}

// Logging interceptor (optional but helpful)
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

// Build OkHttpClient with interceptors
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .build()

// Create Retrofit instance
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.transport.nsw.gov.au/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Define your API interface
val carParkApiService = retrofit.create(CarParkApiService::class.java)