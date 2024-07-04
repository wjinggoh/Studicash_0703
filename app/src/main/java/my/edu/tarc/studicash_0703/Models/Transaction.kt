package my.edu.tarc.studicash_0703.Models

import com.google.firebase.Timestamp


class Transaction {
    var amount: Double = 0.0
    var description: String = ""
    var recordDate: Timestamp = Timestamp.now()
    var transactionType: String = "" // e.g., "income" or "expense"
    var category: String = "" // e.g., "food", "transport", etc.
    var transactionId: String = "" // unique ID for the transaction

    constructor()

    constructor(
        amount:Double,
        description:String,
        recordDate:Timestamp,
        transactionType: String,
        category: String,
        transactionId: String
    ) {
        this.amount = amount
        this.description=description
        this.recordDate=recordDate
        this.transactionType=transactionType
        this.category=category
        this.transactionId=transactionId
    }
}
