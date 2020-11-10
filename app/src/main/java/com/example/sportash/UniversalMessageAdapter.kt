package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class ConversationDetail(data: JSONObject) {
    var SenderID: Int = 0
    var Username: String = ""
    var Count: Int = 0

    init {
        SenderID = data.getInt("SenderID")
        Username = data.getString("Username")
        Count = data.getInt("Count")
    }
}

class UniversalMessageItem(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var mUsername: TextView? = null
    private var mCount: TextView? = null

    init{
        mUsername = itemView.findViewById(R.id.message_username)
        mCount = itemView.findViewById(R.id.message_count)
    }

    fun bind(conversation: ConversationDetail, clickListener: OnConversationClicked){
        mUsername?.text = conversation.Username
        if(conversation.Count < 1){
            mCount?.visibility = View.GONE
        }else if(conversation.Count > 999){
            mCount?.text = "999+"
        }else {
            mCount?.text = conversation.Count.toString()
        }


        itemView.setOnClickListener {
            clickListener.onItemClicked(conversation)
        }
    }
}

class UniversalMessageAdapter(private val conversationList: MutableList<ConversationDetail>, val itemClickListener: OnConversationClicked) : RecyclerView.Adapter<UniversalMessageItem>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversalMessageItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.universal_message_container, parent, false)
        return UniversalMessageItem(view)
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    override fun onBindViewHolder(holder: UniversalMessageItem, position: Int) {
        val conversation = conversationList[position]
        holder.bind(conversation, itemClickListener)
    }

}

interface OnConversationClicked {
    fun onItemClicked(conversation: ConversationDetail)
}

