package com.example.splitmoney.GroupContacts

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contact(
    val name: String,
    val phoneNumber: String,
    var isSelected: Boolean = false
) : Parcelable