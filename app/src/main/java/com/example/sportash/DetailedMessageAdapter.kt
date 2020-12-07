package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class Message(data: JSONObject) {
    var Sender: String = ""
    var Receiver: String = ""
    var Content: String = ""
    var Timestamp: String = ""

    init {
        Sender = data.getString("Sender")
        Receiver = data.getString("Receiver")
        Content = data.getString("Content")
        Timestamp = data.getString("Timestamp")
    }
}

class DetailedMessageItem(itemView: View) : RecyclerView.ViewHolder(itemView){
    private var mSender: TextView? = null
    private var mContent: TextView? = null

    init{
        mSender = itemView.findViewById(R.id.message_sender)
        mContent = itemView.findViewById(R.id.message_content)
    }

    fun bind(message: Message){
        mSender?.text = "${message.Sender}:"
        mContent?.text = message.Content
    }
}

class DetailedMessageAdapter(private val messages: MutableList<Message>) : RecyclerView.Adapter<DetailedMessageItem>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailedMessageItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detailed_message_container, parent, false)
        return DetailedMessageItem(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: DetailedMessageItem, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

}