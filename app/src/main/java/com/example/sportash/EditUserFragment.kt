package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EditUserFragment : Fragment(R.layout.fragment_edit_user) {
    private var user: FullUserDetails? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUser()
        // TODO image view
    }

    private fun populateUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/users/id?id=$userID", null).apply {
                val result = JSONObject(this)
                user = FullUserDetails(
                    result.getInt("ID"),
                    result.getString("Username"),
                    result.getString("Login_Email"),
                    result.getString("Login_Password"),
                    result.getString("FirstName"),
                    result.getString("LastName"),
                    result.getString("DOB")
                )
            }
            withContext(Dispatchers.Main){
                view?.apply {
                    findViewById<TextView>(R.id.edit_user_firstname).text = user?.FirstName
                    findViewById<TextView>(R.id.edit_user_lastname).text = user?.LastName
                    findViewById<TextView>(R.id.edit_user_username).text = user?.Username
                    findViewById<Button>(R.id.edit_user_pick_image).setOnClickListener {
                        browseForImage()
                    }
                    findViewById<Button>(R.id.edit_user_take_image).setOnClickListener {
                        takeImage()
                    }
                    findViewById<Button>(R.id.edit_user_submit).setOnClickListener {
                        submitChanges()
                    }
                }
            }
        }
    }

    private fun browseForImage() {
        // TODO
    }

    private fun takeImage() {
        // TODO
    }

    private fun submitChanges() {
        // TODO
    }
}