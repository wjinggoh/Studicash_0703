package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.core.content.ContextCompat
import my.edu.tarc.studicash_0703.Models.ExpenseCategory
import my.edu.tarc.studicash_0703.Models.IncomeCategory
import my.edu.tarc.studicash_0703.R


class CategorySpinnerAdapter(
    private val context: Context,
    private val categories: List<Any>
) : BaseAdapter() {

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): Any = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun getPosition(category: Any): Int {
        return categories.indexOf(category)
    }


    fun getCategories(): List<Any> = categories

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_items, parent, false)
        val category = categories[position]
        val icon = view.findViewById<ImageView>(R.id.categoryIcon)
        val name = view.findViewById<TextView>(R.id.categoryName)

        when (category) {
            is ExpenseCategory -> {
                if (category.iconUri != null) {
                    Glide.with(context)
                        .load(category.iconUri)
                        .placeholder(R.drawable.img) // Optional placeholder
                        .into(icon)
                } else {
                    icon.setImageDrawable(ContextCompat.getDrawable(context, category.icon))
                }
                name.text = category.name
            }
            is IncomeCategory -> {
                if (category.iconUri != null) {
                    Glide.with(context)
                        .load(category.iconUri)
                        .placeholder(R.drawable.img) // Optional placeholder
                        .into(icon)
                } else {
                    icon.setImageDrawable(ContextCompat.getDrawable(context, category.icon))
                }
                name.text = category.name
            }
        }

        return view
    }
}
