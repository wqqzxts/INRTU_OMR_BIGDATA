package com.example.residentmanagement.data.network

import com.example.residentmanagement.data.model.Publication
import com.example.residentmanagement.data.model.RequestCreateEditPublication
import com.example.residentmanagement.data.model.RequestEditUser
import com.example.residentmanagement.data.model.RequestLogin
import com.example.residentmanagement.data.model.RequestRefreshAccessToken
import com.example.residentmanagement.data.model.RequestRegister
import com.example.residentmanagement.data.model.ResponseLogin
import com.example.residentmanagement.data.model.ResponseRefreshAccessToken
import com.example.residentmanagement.data.model.User

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ApiService  {
    @POST("/api/login/")
    suspend fun loginUser(@Body request: RequestLogin): Response<ResponseLogin>

    @POST("/api/register/")
    suspend fun registerUser(@Body request: RequestRegister): Response<RequestRegister>

    @POST("/api/refresh/")
    fun refreshTokenSync(@Body request: RequestRefreshAccessToken): Call<ResponseRefreshAccessToken>

    @POST("/api/refresh/")
    suspend fun refreshToken(@Body request: RequestRefreshAccessToken): Response<ResponseRefreshAccessToken>

    @POST("/api/publications/")
    suspend fun createPublication(@Body request: RequestCreateEditPublication): Response<Void>

    @GET("/api/publications/")
    suspend fun getPublications(): Response<List<Publication>>

    @GET("/api/publications/{publication_id}/")
    suspend fun getSpecificPublication(@Path("publication_id") publicationId: Int): Response<Publication>

    @PATCH("/api/publications/{publication_id}/")
    suspend fun updateSpecificPublication(
        @Path("publication_id") publicationId: Int,
        @Body request: RequestCreateEditPublication
    ): Response<Publication>

    @DELETE("api/publications/{publication_id}/")
    suspend fun deleteSpecificPublication(@Path("publication_id") publicationId: Int): Response<Void>

    @GET("api/profile/")
    suspend fun getProfileInfo(): Response<User>

    @PATCH("/api/profile/edit/")
    suspend fun updateProfileInfo(@Body request: RequestEditUser): Response<Void>

    @POST("api/logout/")
    suspend fun logoutUser(): Response<Void>
}