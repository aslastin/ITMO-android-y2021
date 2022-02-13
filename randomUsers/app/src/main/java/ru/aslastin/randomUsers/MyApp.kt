package ru.aslastin.randomUsers

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

sealed class ResponseWrapper<out T> {
    data class Success<out T>(val code: Int, val msg: String, val value: T) : ResponseWrapper<T>()
    data class GenericError(val code: Int, val msg: String) : ResponseWrapper<Nothing>()
    object NetworkError : ResponseWrapper<Nothing>()
}

class MyApp : Application() {
    lateinit var service: FakeApiService
        private set

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        service = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FakeApiService::class.java)
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ResponseWrapper<T> =
        withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiCall.invoke()
                if (apiResponse.isSuccessful) {
                    ResponseWrapper.Success(
                        apiResponse.code(),
                        apiResponse.message(),
                        apiResponse.body()!!
                    )
                } else {
                    ResponseWrapper.GenericError(apiResponse.code(), apiResponse.message())
                }
            } catch (e: IOException) {
                ResponseWrapper.NetworkError
            } catch (e: Throwable) {
                ResponseWrapper.GenericError(-1, "Unknown error :(")
            }
        }

    suspend fun serviceGetUsers() = safeApiCall {
        service.getUsers(USERS_LIMIT)
    }

    suspend fun serviceAddRandomUser() = safeApiCall {
        service.addUser(
            User(
                id = -1, // IT DOESN'T MATTER HERE
                userId = Utils.getRandomInt(upperBound = MAX_USERS_COUNT),
                title = Utils.getRandomString(Utils.getRandomInt(MIN_USER_TITLE, MAX_USER_TITLE)),
                body = Utils.getRandomString(Utils.getRandomInt(MIN_USER_BODY, MAX_USER_BODY))
            )
        )
    }

    suspend fun serviceDeleteUser(user: User) = safeApiCall {
        service.deleteUser(user.id)
    }

    companion object {
        lateinit var INSTANCE: MyApp
            private set

        private const val BASE_URL = "https://jsonplaceholder.typicode.com"

        // Service
        private const val USERS_LIMIT = 20

        private const val MAX_USERS_COUNT = 100

        private const val MIN_USER_TITLE = 5
        private const val MAX_USER_TITLE = 30

        private const val MIN_USER_BODY = 10
        private const val MAX_USER_BODY = 50
    }
}
