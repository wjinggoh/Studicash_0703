package my.edu.tarc.studicash_0703.Models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseCategory(
    val icon: Int,
    val name: String,
    val iconUri: String? = null,
    val id: String = ""
) : Parcelable {
    override fun toString(): String {
        return name
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "icon" to icon,
            "iconUri" to (iconUri ?: "") // Ensure the field is named iconUri and not iconUrl
        )
    }
}
