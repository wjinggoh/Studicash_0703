package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Receipt(
    var total: String = "",
    var tax: String = "",
    var type: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "",
    val imageUri: String? = null,  // Add imageUri field if applicable
    val id: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now(),
        parcel.readString() ?: "",
        parcel.readString(), // For imageUri if you have it
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(total)
        parcel.writeString(tax)
        parcel.writeString(type)
        parcel.writeParcelable(timestamp, flags)
        parcel.writeString(userId)
        parcel.writeString(imageUri)  // Add imageUri field if applicable
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Receipt> {
        override fun createFromParcel(parcel: Parcel): Receipt {
            return Receipt(parcel)
        }

        override fun newArray(size: Int): Array<Receipt?> {
            return arrayOfNulls(size)
        }
    }
}
