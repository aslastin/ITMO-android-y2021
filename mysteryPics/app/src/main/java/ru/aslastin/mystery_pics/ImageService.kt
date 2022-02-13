package ru.aslastin.mystery_pics

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Binder
import android.os.IBinder
import android.util.LruCache
import kotlinx.coroutines.*
import java.net.URL
import kotlin.math.min
import kotlin.math.sqrt

class ImageService : Service() {
    companion object {
        private const val CACHE_SIZE = 128 * 1024 * 1024 // 128MiB

        private const val MAX_BITMAP_BYTE_COUNT = 10_000_000
        private const val DESIRED_BITMAP_CNT = 10

        private const val REDUCED_BITMAP_WIDTH = 75
        private const val REDUCED_BITMAP_HEIGHT = REDUCED_BITMAP_WIDTH
    }

    private lateinit var scope: CoroutineScope

    private lateinit var bitmapCache: LruCache<String, Pair<Bitmap, Bitmap>>
    private lateinit var jobByUrl: MutableMap<String, Job>

    override fun onCreate() {
        scope = CoroutineScope(Dispatchers.Main)

        bitmapCache = object : LruCache<String, Pair<Bitmap, Bitmap>>(CACHE_SIZE) {
            override fun sizeOf(key: String?, value: Pair<Bitmap, Bitmap>?): Int {
                return (value?.first?.byteCount ?: 0) + (value?.second?.byteCount ?: 0)
            }
        }

        jobByUrl = mutableMapOf()
    }

    override fun onDestroy() {
        scope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    private fun getResizedBitmap(bm: Bitmap, scaleWidth: Float, scaleHeight: Float): Bitmap {
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(
            bm, 0, 0, bm.width, bm.height,
            matrix, false
        )
    }

    private fun getFullBitmap(rawBitmap: Bitmap): Bitmap {
        val desiredByteCount = min(MAX_BITMAP_BYTE_COUNT, CACHE_SIZE / DESIRED_BITMAP_CNT)
        val scale = sqrt(desiredByteCount.toFloat() / (4 * rawBitmap.width * rawBitmap.height))
        return getResizedBitmap(rawBitmap, scale, scale)
    }

    private fun getReducedBitmap(bitmap: Bitmap): Bitmap {
        return getResizedBitmap(
            bitmap,
            REDUCED_BITMAP_WIDTH.toFloat() / bitmap.width,
            REDUCED_BITMAP_HEIGHT.toFloat() / bitmap.height
        )
    }

    private suspend fun downloadAndCache(url: String) {
        val bitmaps: Pair<Bitmap, Bitmap>? = withContext(Dispatchers.IO) {
            try {
                val bitmapRaw = BitmapFactory.decodeStream(
                    URL(url).openConnection().run {
                        doInput = true
                        connect()
                        getInputStream()
                    }
                )
                val fullBitmap = getFullBitmap(bitmapRaw)
                val reducedBitmap = getReducedBitmap(fullBitmap)
                Pair(fullBitmap, reducedBitmap)
            } catch (e: Throwable) {
                null
            }
        }
        if (bitmaps != null) {
            bitmapCache.put(url, bitmaps)
        }
    }

    inner class LocalBinder : Binder() {
        suspend fun getBitmaps(url: String): Pair<Bitmap, Bitmap>? {
            if (bitmapCache.get(url) == null) {
                if (!jobByUrl.containsKey(url)) {
                    jobByUrl[url] = scope.launch { downloadAndCache(url) }
                }
                jobByUrl[url]?.join()
                jobByUrl.remove(url)
            }
            return bitmapCache.get(url)
        }

        fun hasBitmaps(url: String) = bitmapCache[url] != null
    }

}
