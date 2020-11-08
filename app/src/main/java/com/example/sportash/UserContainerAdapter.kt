package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var uUsername: TextView? = null
    private var uAddButton: Button? = null
    private var uImage: ImageView? = null

    init{
        uUsername = itemView.findViewById(R.id.user_details_username)
        uAddButton = itemView.findViewById(R.id.user_details_button)
    }

    fun bind(user: BaseUserDetails, clickListener: OnUserClicked){
        uUsername?.text = user.username
        if(user.befriended == null) {
            uAddButton?.visibility = View.GONE
        }else {
            uAddButton?.visibility = View.VISIBLE
            uAddButton?.isEnabled = !user.befriended!!
        }

        uAddButton?.setOnClickListener {
            clickListener.onAddButtonClicked(user.id)
            uAddButton?.isEnabled = false
        }
        itemView.setOnClickListener {
            clickListener.onItemClicked(user.id)
        }
    }
}

class UserDetailsAdapter(val userList: MutableList<BaseUserDetails>, val itemClickListener: OnUserClicked) : RecyclerView.Adapter<UserDetailsViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_detials_item, parent, false)
        return UserDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserDetailsViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user, itemClickListener)
    }

}

interface OnUserClicked {
    fun onAddButtonClicked(id: Int)
    fun onItemClicked(id: Int)
}