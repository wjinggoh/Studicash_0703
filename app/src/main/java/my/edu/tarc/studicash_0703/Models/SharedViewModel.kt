package my.edu.tarc.studicash_0703.Models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Define a LiveData property
    private val _category = MutableLiveData<String>()
    val category: LiveData<String> get() = _category

    // Method to update the category
    fun setCategory(newCategory: String) {
        _category.value = newCategory
    }
    private var transactionId: String? = null

    fun setTransactionId(id: String) {
        Log.d("SharedViewModel", "Setting transaction ID: $id")
        transactionId = id
    }

    fun getTransactionId(): String? {
        val id = transactionId
        Log.d("SharedViewModel", "Getting transaction ID: $id")
        return id
    }



}
