package com.example.sportash

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.LocalDateTime

private const val FRIEND_ID = "Friend_ID"

class ConversationsFragment : Fragment(R.layout.fragment_conversations) {
    var userID: Int = 0
    var friendID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            friendID = it.getInt(FRIEND_ID)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(friendID: Int) =
            ConversationsFragment().apply {
                arguments = Bundle().apply {
                    putInt(FRIEND_ID, friendID)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.GONE

        // friendID is passed value from fragment messages
        userID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0) ?: 0
        if(userID == 0){
            Toast.makeText(context, getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
            return
        }

        // Load conversation history for the user and friend
        retrieveConversationHistory()

        // Send message load confirmation
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.PUT, "${SportashAPI.apiURL}/MessagesReads?userID=$userID&friendID=$friendID", null)
        }

        view.findViewById<Button>(R.id.send_message).setOnClickListener{
            sendMessage()
        }

    }

    private fun retrieveConversationHistory(){
        CoroutineScope(Dispatchers.IO).launch {
            var messages = mutableListOf<Message>()
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/Messages?userID=$userID&friendID=$friendID", null).apply {
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    messages.add(Message(results.getJSONObject(i)))
                }
                withContext(Dispatchers.Main){
                    populateConversationHistory(messages)
                }
            }
        }
    }

    private fun populateConversationHistory(messages: MutableList<Message>) {
        view?.findViewById<EditText>(R.id.message_edit_content)?.setText("")
        view?.findViewById<RecyclerView>(R.id.conversation_track_container)?.apply {
            this?.layoutManager = LinearLayoutManager(context)
            this?.adapter = DetailedMessageAdapter(messages)
        }
        view?.findViewById<RecyclerView>(R.id.conversation_track_container)?.scrollToPosition(messages.size - 1)
    }

    data class MessageRequest(val SenderID: Int, val ReceiverID: Int, val Content: String, val Timestamp: String)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(){
        CoroutineScope(Dispatchers.IO).launch{
            val body = MessageRequest(
                userID,
                friendID,
                view?.findViewById<EditText>(R.id.message_edit_content)?.text.toString(),
                LocalDateTime.now().toString()
            )
            SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/Messages", body)
            withContext(Dispatchers.Main){
                retrieveConversationHistory()
            }
        }
    }
}