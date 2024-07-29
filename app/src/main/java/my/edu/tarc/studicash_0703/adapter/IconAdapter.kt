package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import my.edu.tarc.studicash_0703.R

class IconAdapter(private val context: Context, private val icons: Array<Int>) : BaseAdapter() {

    override fun getCount(): Int = icons.size

    override fun getItem(position: Int): Any = icons[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.iconImageView.setImageResource(icons[position])
        return view
    }

    private class ViewHolder(view: View) {
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
    }
}
