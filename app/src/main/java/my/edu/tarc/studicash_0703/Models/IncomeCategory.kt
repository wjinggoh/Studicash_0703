package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable

data class IncomeCategory(
    val icon: Int,
    val name: String,
    val iconUri: String? = null,
    val id: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(icon)
        parcel.writeString(name)
        parcel.writeString(iconUri)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IncomeCategory> {
        override fun createFromParcel(parcel: Parcel): IncomeCategory {
            return IncomeCategory(parcel)
        }

        override fun newArray(size: Int): Array<IncomeCategory?> {
            return arrayOfNulls(size)
        }
    }
}
