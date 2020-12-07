package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MessagesFragment : Fragment(R.layout.fragment_messages) {
    var userID: Int = 0

    override fun onDetach() {
        super.onDetach()
        view?.findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0) ?: 0
        if(userID == 0) {
            Toast.makeText(context, getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
            return
        }

        view?.findViewById<SearchView>(R.id.search_for_conversation)?.setOnQueryTextListener (
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    var userList = mutableListOf<BaseUserDetails>()
                    newText?:
                    CoroutineScope(Dispatchers.IO).launch {
                        SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/users/search?query=$newText", null).run {
                            val results = JSONArray(this)
                            for(i in 0 until results.length()) {
                                userList.add(BaseUserDetails(results.getJSONObject(i)))
                            }
                            withContext(Dispatchers.Main){
                                view?.findViewById<RecyclerView>(R.id.conversations_container).apply {
                                    this.layoutManager = LinearLayoutManager(context)
                                    this.adapter = UserDetailsAdapter(userList, object: OnUserClicked {
                                        override fun onAddButtonClicked(id: Int) {}

                                        override fun onItemClicked(id: Int) {
                                            openConversationDetail(id)
                                        }

                                    })
                                }
                            }
                        }
                    }
                    return true
                }
            })

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
                    openConversationDetail(conversation.SenderID)
                }
            })
            this?.scrollToPosition(items.count())
        }
    }

    private fun openConversationDetail(senderID: Int){
        val fragment = ConversationsFragment.newInstance(senderID)
        activity?.supportFragmentManager?.beginTransaction()
            ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)?.addToBackStack("CONVERSATION")
            ?.replace(R.id.main_fragment_container, fragment)?.commit()
    }
}