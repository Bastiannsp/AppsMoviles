package com.example.gamezone.network

import com.example.gamezone.network.dto.AuthResponseDto
import com.example.gamezone.network.dto.LoginRequestDto
import com.example.gamezone.network.dto.RegisterRequestDto
import com.example.gamezone.network.dto.UpdateUserRequestDto
import com.example.gamezone.network.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define los endpoints expuestos por el backend de Gamezone.
 */
interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UserResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @GET("api/users/{email}")
    suspend fun getUser(@Path("email") email: String): Response<UserResponseDto>

    @PUT("api/users/{email}")
    suspend fun updateUser(
        @Path("email") email: String,
        @Body request: UpdateUserRequestDto
    ): Response<UserResponseDto>
}