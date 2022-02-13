package ru.aslastin.random_users_db

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val service: UserService,
    private val userDao: UserDao
) {
    var users: Flow<List<User>> = userDao.getAll()

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        onSuccess: suspend (T) -> Unit,
    ): ResponseWrapper<T> =
        try {
            val apiResponse = apiCall.invoke()
            if (apiResponse.isSuccessful) {
                val body = apiResponse.body()!!
                onSuccess.invoke(body)
                ResponseWrapper.Success(apiResponse.code(), apiResponse.message(), body)
            } else {
                ResponseWrapper.GenericError(apiResponse.code(), apiResponse.message())
            }
        } catch (e: IOException) {
            ResponseWrapper.NetworkError
        } catch (e: Throwable) {
            ResponseWrapper.GenericError(-1, "Unknown error :(")
        }

    suspend fun addRandomUser() =
        safeApiCall({ service.addUser(initRandomUser()) }) { resUser ->
            resUser.id = 0 // make id default so db can manage it
            userDao.insertAll(resUser)
        }

    private fun initRandomUser() = User(
        userId = Utils.getRandomInt(upperBound = MAX_USERS_COUNT),
        title = Utils.getRandomString(Utils.getRandomInt(MIN_USER_TITLE, MAX_USER_TITLE)),
        body = Utils.getRandomString(Utils.getRandomInt(MIN_USER_BODY, MAX_USER_BODY))
    )

    suspend fun deleteUser(user: User) =
        safeApiCall({ service.deleteUser(user.id) }) {
            userDao.delete(user)
        }

    suspend fun resetUsers() =
        safeApiCall({ service.getUsers(getUsersStart(), USERS_LIMIT) }) {
            userDao.deleteAll()
            userDao.insertAll(*it.toTypedArray())
        }

    suspend fun initUsers() =
        safeApiCall({ service.getUsers(getUsersStart(), USERS_LIMIT) }) {
            userDao.insertAll(*it.toTypedArray())
        }

    private fun getUsersStart() = Utils.getRandomInt(upperBound = MAX_USERS_COUNT - USERS_LIMIT)

    companion object {
        private const val USERS_LIMIT = 20

        private const val MAX_USERS_COUNT = 100

        private const val MIN_USER_TITLE = 5
        private const val MAX_USER_TITLE = 30

        private const val MIN_USER_BODY = 10
        private const val MAX_USER_BODY = 50
    }
}
