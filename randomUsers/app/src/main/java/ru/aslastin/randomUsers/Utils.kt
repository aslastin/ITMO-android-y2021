package ru.aslastin.randomUsers

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
