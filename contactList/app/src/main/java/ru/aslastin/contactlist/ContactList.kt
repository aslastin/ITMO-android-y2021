package ru.aslastin.contactlist

import android.annotation.SuppressLint

import android.content.Context
import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.CommonDataKinds.Phone
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contact(val name: String, val phoneNumber: String) : Parcelable

@SuppressLint("Range")
fun Context.fetchAllContacts(): List<Contact> {
    contentResolver.query(CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        .use { cursor ->
            if (cursor == null) {
                return emptyList()
            }
            val wasName = HashSet<String>()
            val builder = ArrayList<Contact>()
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)) ?: "N/A"
                if (wasName.contains(name)) {
                    continue
                }
                val phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER)) ?: "N/A"
                wasName.add(name)
                builder.add(Contact(name, phoneNumber))
            }
            return builder.sortedBy { it.name }
        }
}
