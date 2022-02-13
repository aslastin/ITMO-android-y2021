package ru.aslastin.mystery_pics

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageData(
    val description: String,
    val url: String,
    var bitmap: Bitmap? = null
) : Parcelable
