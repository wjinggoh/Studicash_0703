package my.edu.tarc.studicash_0703.Models

import androidx.lifecycle.ViewModel

class TransactionViewModel : ViewModel() {
    private var transactionId: String? = null

    fun setTransactionId(id: String) {
        transactionId = id
    }

    fun getTransactionId(): String? {
        return transactionId
    }
}
