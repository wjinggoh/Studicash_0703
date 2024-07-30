package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class BudgetItem(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("name") val name: String = "",
    @get:PropertyName("category") val category: String = "",
    @get:PropertyName("amount") val amount: Double = 0.0,
    @get:PropertyName("spent") var spent: Double = 0.0,
    @get:PropertyName("progress") var progress: Int = 0,
    @get:PropertyName("startDate") val startDate: String = "",
    @get:PropertyName("endDate") val endDate: String = "",
    @get:PropertyName("icon") val icon: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeDouble(amount)
        parcel.writeDouble(spent)
        parcel.writeInt(progress)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeInt(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BudgetItem> {
        override fun createFromParcel(parcel: Parcel): BudgetItem {
            return BudgetItem(parcel)
        }

        override fun newArray(size: Int): Array<BudgetItem?> {
            return arrayOfNulls(size)
        }
    }
}
