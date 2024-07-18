package my.edu.tarc.studicash_0703.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import my.edu.tarc.studicash_0703.R

class IconAdapter(private val context: Context, private val iconUris: Array<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return iconUris.size
    }

    override fun getItem(position: Int): Any {
        return iconUris[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Load icon using Glide
        Glide.with(context)
            .load(iconUris[position])
            .into(viewHolder.iconImageView)

        return view
    }

    private class ViewHolder(view: View) {
        val iconImageView: ImageView = view.findViewById(R.id.iconImageView)
    }
}
