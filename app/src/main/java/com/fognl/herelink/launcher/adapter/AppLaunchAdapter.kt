package com.fognl.herelink.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fognl.herelink.launcher.R
import com.fognl.herelink.launcher.model.AppLaunch

class AppLaunchHolder(itemView: View, private val itemListener: AppLaunchAdapter.ItemListener): RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup, listener: AppLaunchAdapter.ItemListener): AppLaunchHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_applaunch, parent, false)
            return AppLaunchHolder(view, listener)
        }
    }

    private val title: TextView = itemView.findViewById(R.id.txt_title)
    private val appIcon: ImageView = itemView.findViewById(R.id.img_icon)

    private var itemPosition: Int = 0
    private var item: AppLaunch? = null

    init {
        itemView.setOnClickListener {
            item?.let { item ->
                itemListener.onItemClicked(itemPosition, item)
            }
        }

        itemView.setOnLongClickListener {
            item?.let { item ->
                itemListener.onItemLongPressed(itemPosition, item)
            }

            true
        }
    }

    fun bind(position: Int, item: AppLaunch) {
        this.itemPosition = position
        this.item = item

        title.setText(item.title ?: "no title")
        item.icon?.let { ic ->
            appIcon.setImageDrawable(ic)
        }
    }
}

class AppLaunchAdapter(val items: List<AppLaunch>, val itemListener: ItemListener): RecyclerView.Adapter<AppLaunchHolder>() {

    interface ItemListener {
        fun onItemClicked(position: Int, item: AppLaunch)
        fun onItemLongPressed(position: Int, item: AppLaunch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppLaunchHolder {
        return AppLaunchHolder.newInstance(parent, itemListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: AppLaunchHolder, position: Int) {
        holder.bind(position, items[position])
    }

    fun isEmpty(): Boolean = items.isEmpty()
}
