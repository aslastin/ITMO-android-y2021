package ru.aslastin.random_users_db

import java.util.*

object Utils {
    private const val ALLOWED_CHARACTERS = "0123456789    qwertyuiopasdfghjklzxcvbnm"
    private val random = Random()

    // All bounds inclusive
    fun getRandomInt(lowerBound: Int = 0, upperBound: Int) =
        lowerBound + random.nextInt(upperBound + 1)

    fun getRandomString(size: Int): String {
        return generateSequence { ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)] }
            .take(size)
            .joinToString(separator = "")
    }
}

data class OperationInfo(
    val type: OperationInfoType,
    val code: Int? = null,
    val msg: String? = null
);

enum class OperationInfoType {
    SUCCESS, GENERIC_ERROR, NETWORK_ERROR;
}

enum class OperationType {
    ADD, DELETE, RESET
}

sealed class ResponseWrapper<out T> {
    data class Success<out T>(val code: Int, val msg: String, val value: T) : ResponseWrapper<T>() {
        override fun toOperationInfo() = OperationInfo(OperationInfoType.SUCCESS, code, msg)
    }

    data class GenericError(val code: Int, val msg: String) : ResponseWrapper<Nothing>() {
        override fun toOperationInfo() = OperationInfo(OperationInfoType.GENERIC_ERROR, code, msg)
    }

    object NetworkError : ResponseWrapper<Nothing>() {
        override fun toOperationInfo() = OperationInfo(OperationInfoType.NETWORK_ERROR)
    }

    abstract fun toOperationInfo(): OperationInfo
}
