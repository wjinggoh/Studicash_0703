package my.edu.tarc.studicash_0703.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import my.edu.tarc.studicash_0703.Models.CategoryItem
import my.edu.tarc.studicash_0703.R

class CategorySpinnerAdapter(
    private val context: Context,
    private val categories: List<CategoryItem>
) : BaseAdapter() {

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_items, parent, false)

        val category = categories[position]
        val icon = view.findViewById<ImageView>(R.id.category)
        val name = view.findViewById<TextView>(R.id.text)

        icon.setImageDrawable(ContextCompat.getDrawable(context, category.icon))
        name.text = category.name

        return view
    }
}
