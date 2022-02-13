package ru.aslastin.random_users_db

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("posts")
    suspend fun getUsers(
        @Query("_start") start: Int,
        @Query("_limit") limit: Int
    ): Response<List<User>>

    @POST("posts")
    suspend fun addUser(@Body newUser: User): Response<User>

    @DELETE("posts/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://jsonplaceholder.typicode.com"
    }
}
