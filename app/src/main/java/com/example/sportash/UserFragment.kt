package com.example.sportash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

private const val USER_ID = "USER_ID"
private const val USER_AUTH = "USER_AUTH"

class UserFragment : Fragment(R.layout.fragment_user) {

    companion object {
        @JvmStatic
        fun newInstance(userID: Int, isOwner: Boolean) = UserFragment().apply {
            arguments = Bundle().apply {
                putInt(USER_ID, userID)
                putBoolean(USER_AUTH, isOwner)
            }
        }
    }

    var userID: Int = 0
    var isOwner: Boolean = false
    var fullUserDetails: FullUserDetails? = null
    var userSkill: MutableList<Skill> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userID = it.getInt(USER_ID)
            isOwner = it.getBoolean(USER_AUTH)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userID == 0){
            Toast.makeText(context, getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
        }
        // If user wants to open the details of another user that is not himself
        if(!isOwner){
            view.findViewById<Button>(R.id.btn_edit_profile).visibility = View.GONE
            view.findViewById<Button>(R.id.btn_change_pswrd).visibility = View.GONE
            view.findViewById<Button>(R.id.btn_logout).visibility = View.GONE
            view.findViewById<EditText>(R.id.edit_new_skill).visibility = View.GONE
            view.findViewById<Button>(R.id.btn_add_skill).visibility = View.GONE
            view.findViewById<TextView>(R.id.txt_label_new_skill).visibility = View.GONE
        }
        else {
            view.findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
                activity?.supportFragmentManager?.apply {
                    beginTransaction().replace(R.id.main_fragment_container, EditUserFragment()).addToBackStack("EDIT_USER").commit()
                }
            }
            view.findViewById<Button>(R.id.btn_change_pswrd).setOnClickListener {
                // TODO: request password change
            }
            view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
                activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.edit()?.putInt(SportashAPI.USER_ID, 0)?.apply()
                activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.edit()?.putBoolean(SportashAPI.USER_STORED, false)?.apply()
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
            }
            view.findViewById<Button>(R.id.btn_add_skill).setOnClickListener{
                val skillText = view.findViewById<EditText>(R.id.edit_new_skill).text
                CoroutineScope(Dispatchers.IO).launch {
                    SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/User_Skill?userID=$userID&text=$skillText", null)
                    populateSkills()
                }
            }
        }
        // Populate containers
        populateDetails()
        populateSkills()
    }

    private fun populateDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/users/id?id=$userID", null).apply {
                val result = JSONObject(this)
                fullUserDetails = FullUserDetails(
                    result.getInt("ID"),
                    result.getString("Image"),
                    result.getString("Username"),
                    result.getString("Login_Email"),
                    result.getString("Login_Password"),
                    result.getString("FirstName"),
                    result.getString("LastName"),
                    result.getString("DOB")
                )
                withContext(Dispatchers.Main){
                    fullUserDetails?.apply{
                        view?.findViewById<TextView>(R.id.txt_username)?.text = Username
                        view?.findViewById<TextView>(R.id.txt_name_surname)?.text = "$FirstName $LastName"
                        view?.findViewById<TextView>(R.id.txt_email)?.text = Login_Email
                        SportashAPI.setImage(fullUserDetails?.Image, view?.findViewById(R.id.user_image)!!)
                    }
                }
            }
        }
    }

    private fun populateSkills() {
        CoroutineScope(Dispatchers.IO).launch{
            userSkill = mutableListOf()
            val viewerID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0)
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/User_Skill?viewerID=$viewerID&userID=$userID", null).apply {
                val result = JSONArray(this)
                for(i in 0 until result.length()){
                    val skill = result.getJSONObject(i)
                    userSkill.add(Skill(
                        skill.getInt("ID"),
                        skill.getInt("UserID"),
                        skill.getString("Skill"),
                        skill.getInt("EndorsmentCount"),
                        skill.getBoolean("Endorsed")
                    ))
                }
            }
            withContext(Dispatchers.Main){
                view?.findViewById<RecyclerView>(R.id.skillContainer)?.apply {
                    this.layoutManager = LinearLayoutManager(context)
                    val listener = object : OnSkillClickedListener {
                        override fun onEndorsmentClicked(skill: Skill) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val viewerID = activity?.getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0)
                                if(skill.Endorsed){
                                    try{
                                        SportashAPI.HTTPRequest(SportashAPI.DELETE, "${SportashAPI.apiURL}/SkillEndorsments?userID=$viewerID&userSkillID=${skill.ID}",null)
                                    } catch (ex: Exception){
                                        // When it is already liked/unliked, but unsynced
                                    }
                                }
                                else {
                                    try {
                                        SportashAPI.HTTPRequest(SportashAPI.POST,"${SportashAPI.apiURL}/SkillEndorsments?userID=$viewerID&userSkillID=${skill.ID}", null)
                                    } catch (ex: Exception){
                                        // When it is already liked/unliked, but unsynced
                                    }
                                }
                                populateSkills()
                            }
                        }
                        override fun onRemovedClicked(skill: Skill) {
                            CoroutineScope(Dispatchers.IO).launch {
                                SportashAPI.HTTPRequest(SportashAPI.DELETE, "${SportashAPI.apiURL}/User_Skill/${skill.ID}", null)
                                populateSkills()
                            }
                        }
                    }
                    this.adapter = SkillAdapter(userSkill, listener, isOwner)
                }
            }
        }
    }
}