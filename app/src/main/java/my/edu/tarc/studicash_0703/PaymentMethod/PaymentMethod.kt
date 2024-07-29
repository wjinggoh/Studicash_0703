package my.edu.tarc.studicash_0703.PaymentMethod

import android.os.Parcel
import android.os.Parcelable

data class PaymentMethod(
    val type: String = "",
    val details: String = "",
    val formattedDetails: String = "",
    val uid: String = "" // Add uid field to associate with user
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "" // Read uid from parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(details)
        parcel.writeString(formattedDetails)
        parcel.writeString(uid) // Write uid to parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentMethod> {
        override fun createFromParcel(parcel: Parcel): PaymentMethod {
            return PaymentMethod(parcel)
        }

        override fun newArray(size: Int): Array<PaymentMethod?> {
            return arrayOfNulls(size)
        }
    }
}
