package ru.aslastin.randomUsers

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FakeApiService {
    @GET("posts")
    suspend fun getUsers(@Query("_limit") limit: Int): Response<MutableList<User>>

    @POST("posts")
    suspend fun addUser(@Body newUser: User): Response<User>

    @DELETE("posts/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ResponseBody>
}
