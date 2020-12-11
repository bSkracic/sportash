package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var tText : TextView? = null
    private var tRemove : TextView? = null

    init {
        tText = itemView.findViewById(R.id.tag_text)
        tRemove = itemView.findViewById(R.id.tag_remove)
    }

    fun bind(tag: String, index: Int, clickListener: OnTagClicked) {
        tText?.text = tag
        tRemove?.setOnClickListener {
            clickListener.onRemoveClicked(index)
        }
    }
}

class TagAdapter(private val tags: MutableList<String>, private val clickListener: OnTagClicked) : RecyclerView.Adapter<TagItem>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        return TagItem(view)
    }

    override fun onBindViewHolder(holder: TagItem, position: Int) {
        holder.bind(tags[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return tags.size
    }

}

interface OnTagClicked {
    fun onRemoveClicked(index: Int)
}
