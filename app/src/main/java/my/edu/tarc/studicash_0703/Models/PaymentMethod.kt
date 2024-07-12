package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable

class PaymentMethod(
    val type: String = "",
    val details: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    // Parse the details field to extract necessary information
    fun getFormattedDetails(): String {
        // Example logic to extract relevant details
        val parts = details.split(",")
        val formattedDetails = parts.joinToString(separator = ",") { part ->
            part.trim().substringAfter(":").trim()
        }
        return formattedDetails
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(details)
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
