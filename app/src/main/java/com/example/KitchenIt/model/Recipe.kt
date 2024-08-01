package com.example.KitchenIt

import android.os.Parcel
import android.os.Parcelable

data class Recipe(
    val title: String,
    val content: String,
    val imageUrl: String,
    val products: String,
    val userEmail: String = "default@user",
    val timestamp: Long = System.currentTimeMillis(),

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(imageUrl)
        parcel.writeString(products)
        parcel.writeString(userEmail)
        parcel.writeLong(timestamp)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }
}
