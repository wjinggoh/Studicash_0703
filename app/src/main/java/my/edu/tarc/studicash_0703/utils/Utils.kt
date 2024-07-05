package my.edu.tarc.studicash_0703.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import my.edu.tarc.studicash_0703.Models.Expense

fun uploadImage(uri: Uri, folderName:String, callback:(String?)->Unit){
    var imageUrl:String?=null
    FirebaseStorage.getInstance().getReference(folderName).
    child(UUID.randomUUID().toString()).putFile(uri)
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                imageUrl=it.toString()
                callback(imageUrl)
            }
        }
}

fun saveExpense(expense:Expense) {
    val db = Firebase.firestore

    db.collection("expenses")
        .add(expense)
        .addOnSuccessListener { documentReference ->
            println("Expense added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            println("Error adding transaction: $e")
        }
}