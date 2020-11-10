package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MessagesFragment : Fragment(R.layout.fragment_messages) {
    var userID: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0) ?: 0
        if(userID == 0){
            Toast.makeText(context, getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
            return
        }

        val conversations: MutableList<ConversationDetail> = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(
                SportashAPI.GET,
                "${SportashAPI.apiURL}/MessagesReads/$userID",
                null
            ).apply {
                val results = JSONArray(this)
                for (i in 0 until results.length()) {
                    conversations.add(ConversationDetail(results.getJSONObject(i)))
                }
                withContext(Dispatchers.Main){
                    populateConversations(conversations)
                }
            }
        }

    }

    private fun populateConversations(items: MutableList<ConversationDetail>){
        view?.findViewById<RecyclerView>(R.id.conversations_container).apply {
            this?.layoutManager = LinearLayoutManager(context)
            this?.adapter = UniversalMessageAdapter(items, object: OnConversationClicked {
                override fun onItemClicked(conversation: ConversationDetail) {
                    Toast.makeText(context, "click test", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}

