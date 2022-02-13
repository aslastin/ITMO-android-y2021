package ru.aslastin.randomUsers

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) : Parcelable
