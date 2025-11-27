package com.example.gamezone.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // IMPORTANTE: Reemplaza esta URL con la dirección base de tu backend.
    // "http://10.0.2.2:8080/" es la dirección para conectar al localhost de tu PC
    // desde el emulador de Android. Si usas un dispositivo físico, usa la IP de tu PC.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}