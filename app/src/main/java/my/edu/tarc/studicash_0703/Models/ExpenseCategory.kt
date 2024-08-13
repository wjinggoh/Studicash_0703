package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseCategory(
    val icon: Int, // This should be an Int for resource IDs
    val name: String,
    val iconUri: String? = null, // This should be a String? for URIs
    val id: String = "",
    val uid: String = ""
) : Parcelable {
    override fun toString(): String {
        return name
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "icon" to icon,
            "iconUri" to (iconUri ?: ""),
            "uid" to uid // Include uid in the map
        )
    }


}
