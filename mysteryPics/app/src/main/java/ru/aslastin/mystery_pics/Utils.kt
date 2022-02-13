package ru.aslastin.mystery_pics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Utils {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }

    object Unsplash {
        private const val CLIENT_ID = "2u8V4Tq9EDJB8GPLlK2gC9jgRZvftt6B1pc55TnTfkU"
        private const val ORIENTATION = "squarish"

        private val REGEX_DESCRIPTION = "\"description\":\"(.*?)\"".toRegex()
        private val REGEX_URL = "\"small\":\"(.*?)\"".toRegex()

        fun getRandomPhotosURL(count: Int = 100): String {
            return "https://api.unsplash.com/photos/random?client_id=$CLIENT_ID&count=$count&orientation=$ORIENTATION";
        }

        fun extractAllImageData(input: String): List<ImageData> {
            val res = ArrayList<ImageData>()
            var startIndex = 0
            while (true) {
                val matchResultDescription = REGEX_DESCRIPTION.find(input, startIndex) ?: break
                val matchResultUrl =
                    REGEX_URL.find(input, matchResultDescription.range.last) ?: break
                val (description) = matchResultDescription.destructured
                val (url) = matchResultUrl.destructured
                res.add(ImageData(description, url))
                startIndex = matchResultUrl.range.last
            }
            return res
        }
    }

}
