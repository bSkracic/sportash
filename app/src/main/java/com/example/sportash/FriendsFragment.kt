package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import org.json.JSONArray

/*
search friends with query, update recycler view with every query submit event
RecyclerView:
| Example user 01 | username, image, ID, button: 'add'
| Example user 02 | ...
| Example user 03 | ...
...
Listeners:
on click item: open member details fragment (User)\
on click button 'add': check if friend request is sent to that user by this user ? disable button : enable button
*/

// create open user details logic, in fragment check if id passed is userID
// remove blocking calls - failed
// guess this part is finished

class FriendsFragment : Fragment(R.layout.fragment_friends) {
    var userID: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve User ID
        userID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0) ?: 0
        if(userID == 0){
            Toast.makeText(context, getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
            return
        }

        // Remove search view in displaying default container
        view.findViewById<SearchView>(R.id.friends_search).visibility = View.GONE

        populateFriends()

        // Main navigation
        view.findViewById<TabLayout>(R.id.friends_nav).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                   when(view.findViewById<TabLayout>(R.id.friends_nav).selectedTabPosition){
                       0 -> {
                           view.findViewById<SearchView>(R.id.friends_search).visibility = View.GONE
                           populateFriends()
                       }
                       1 -> {
                           view.findViewById<SearchView>(R.id.friends_search).visibility = View.VISIBLE
                           populateAddFriends()
                       }
                       2 -> {
                           view.findViewById<SearchView>(R.id.friends_search).visibility = View.GONE
                           populateRequests()
                       }
                   }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun populateFriends() {
        emptyContainer()
        var friendList = mutableListOf<BaseUserDetails>()
        val job = CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/UserFriends/friends?id=$userID", null).run {
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    friendList.add(BaseUserDetails(results.getJSONObject(i)))
                }
            }
        }
        val listener = object: OnUserClicked {
            override fun onAddButtonClicked(id: Int) {
                // nothing
            }

            override fun onItemClicked(id: Int) {
                openUserDetails()
            }
        }
        runBlocking {
            job.join()
            val container = view?.findViewById<RecyclerView>(R.id.friends_container)
            populateContainer(friendList, container, listener)
        }
    }

    data class RelationRequest(val UserID: Int, val FriendUserID: Int)
    private fun populateAddFriends(){
        emptyContainer()
        val listener = object: OnUserClicked {
            override fun onAddButtonClicked(id: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/PendingRequests", RelationRequest(userID, id))
                }
            }

            override fun onItemClicked(id: Int) {
                openUserDetails()
            }
        }
        val container = view?.findViewById<RecyclerView>(R.id.friends_container)
        view?.findViewById<SearchView>(R.id.friends_search)?.setOnQueryTextListener (
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val results = sendQuery(query)
                    populateContainer(results, container, listener)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText?.length!! >= 3){
                        val results = sendQuery(newText)
                        populateContainer(results, container, listener)
                    }
                    return true
                }
            })
    }

    private fun sendQuery(query: String?): MutableList<BaseUserDetails> {
        var userList = mutableListOf<BaseUserDetails>()
        var friendsID= mutableListOf<Int>()
        val job1 = CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/users/search?query=$query", null).run {
                val results = JSONArray(this)
                for(i in 0 until results.length()) {
                    userList.add(BaseUserDetails(results.getJSONObject(i)))
                }
            }
        }

        val job2 = CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/UserFriends/$userID", null).run{
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    friendsID.add(results.getInt(i))
                }
            }
        }
        runBlocking {
            job1.join()
            job2.join()
            userList.forEach{
                it.befriended = friendsID.contains(it.id)
            }
        }
        return userList
    }

    private fun populateRequests() {
        emptyContainer()
        var userList = mutableListOf<BaseUserDetails>()
        val listener = object: OnUserClicked {
            override fun onAddButtonClicked(id: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    SportashAPI.HTTPRequest(SportashAPI.DELETE, "${SportashAPI.apiURL}/PendingRequests/$id", null)
                }
                populateRequests()
            }

            override fun onItemClicked(id: Int) {
                openUserDetails()
            }
        }
        val container = view?.findViewById<RecyclerView>(R.id.friends_container)
        var requests = mutableListOf<BaseUserDetails>()
        val job = CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/PendingRequests/$userID", null).run {
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    val request = BaseUserDetails(results.getJSONObject(i))
                    request.befriended = false
                    requests.add(request)
                }
            }
        }
        runBlocking {
            job.join()
            populateContainer(requests, container, listener)
        }

    }

    private fun populateContainer(userList: MutableList<BaseUserDetails>, container: RecyclerView?, clickListener: OnUserClicked){
        container?.run {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = UserDetailsAdapter(userList, clickListener)
        }
    }

    private fun emptyContainer(){
        populateContainer(mutableListOf(), view?.findViewById(R.id.friends_container), object: OnUserClicked {
            override fun onAddButtonClicked(id: Int) {
            }

            override fun onItemClicked(id: Int) {
            }
        })
    }

    private fun openUserDetails(){
        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
    }
}