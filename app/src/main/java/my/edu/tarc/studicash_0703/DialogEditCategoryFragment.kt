package my.edu.tarc.studicash_0703.Transaction

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.io.Serializable

class DialogEditCategoryFragment : DialogFragment() {

    private lateinit var category: ExpenseCategory
    private var onCategoryUpdated: ((ExpenseCategory) -> Unit)? = null
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_ON_CATEGORY_UPDATED = "onCategoryUpdated"

        fun newInstance(category: ExpenseCategory, onCategoryUpdated: (ExpenseCategory) -> Unit): DialogEditCategoryFragment {
            return DialogEditCategoryFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CATEGORY, category)
                    putSerializable(ARG_ON_CATEGORY_UPDATED, SerializableWrapper(onCategoryUpdated))
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_edit_category, null)
        val iconImageView: ImageView = view.findViewById(R.id.categoryIcon)
        val nameEditText: EditText = view.findViewById(R.id.categoryName)
        val saveButton: ImageButton = view.findViewById(R.id.saveCategoryButton)
        val cancelButton: ImageButton = view.findViewById(R.id.cancelButton)

        arguments?.let {
            category = it.getParcelable(ARG_CATEGORY)!!
            onCategoryUpdated = (it.getSerializable(ARG_ON_CATEGORY_UPDATED) as SerializableWrapper<*>).value as? (ExpenseCategory) -> Unit
        }

        nameEditText.setText(category.name)
        Glide.with(this).load(category.iconUri).into(iconImageView)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)

        saveButton.setOnClickListener {
            val newName = nameEditText.text.toString()
            if (newName.isNotEmpty()) {
                val updatedCategory = category.copy(name = newName)
                updateCategoryInFirestore(updatedCategory)
            } else {
                // Show a toast or error message here if needed
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun updateCategoryInFirestore(updatedCategory: ExpenseCategory) {
        db.collection("ExpenseCategories").document(updatedCategory.id)
            .update("name", updatedCategory.name)
            .addOnSuccessListener {
                onCategoryUpdated?.invoke(updatedCategory)
                dismiss()
            }
            .addOnFailureListener { exception ->
                // Handle error (show a toast or log the error)
            }
    }

    // Serializable wrapper to pass lambda
    private data class SerializableWrapper<T>(val value: T) : Serializable
}
