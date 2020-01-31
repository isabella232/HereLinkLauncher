package com.fognl.herelink.launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fognl.herelink.launcher.R
import java.io.File

class WallpaperHolder(itemView: View, private val listener: WallpaperAdapter.ItemListener) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun newInstance(parent: ViewGroup, listener: WallpaperAdapter.ItemListener): WallpaperHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_wallpaper, parent, false)
            return WallpaperHolder(view, listener)
        }
    }

    val textView: TextView = (itemView as TextView)
    var itemPosition: Int = 0
    var item: File? = null

    init {
        itemView.setOnClickListener {
            item?.let { file ->
                listener.onItemSelected(itemPosition, file)
            }
        }
    }

    fun bind(position: Int, item: File) {
        itemPosition = position
        this.item = item

        textView.text = item.name
    }
}

class WallpaperAdapter(val items: List<File>, val itemListener: ItemListener) : RecyclerView.Adapter<WallpaperHolder>() {
    interface ItemListener {
        fun onItemSelected(position: Int, file: File)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperHolder {
        return WallpaperHolder.newInstance(parent, itemListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: WallpaperHolder, position: Int) {
        holder.bind(position, items[position])
    }
}
