package com.example.sportash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EditUserFragment : Fragment(R.layout.fragment_edit_user) {
    private var user: FullUserDetails? = null

    var imageExists = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUser()
    }

    private fun populateUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/users/id?id=$userID", null).apply {
                val result = JSONObject(this)
                user = FullUserDetails(
                    result.getInt("ID"),
                    result.getString("Image"),
                    result.getString("Username"),
                    result.getString("Login_Email"),
                    result.getString("Login_Password"),
                    result.getString("FirstName"),
                    result.getString("LastName"),
                    result.getString("DOB")
                )
                imageExists = user?.Image != null
            }
            withContext(Dispatchers.Main){
                view?.apply {
                    findViewById<TextView>(R.id.edit_user_firstname).text = user?.FirstName
                    findViewById<TextView>(R.id.edit_user_lastname).text = user?.LastName
                    findViewById<TextView>(R.id.edit_user_username).text = user?.Username
                    SportashAPI.setImage(user?.Image, view?.findViewById(R.id.edit_user_image)!!)
                    findViewById<Button>(R.id.edit_user_pick_image).setOnClickListener {
                       SportashAPI.pickImageFromGallery(this@EditUserFragment)
                    }
                    findViewById<Button>(R.id.edit_user_take_image).setOnClickListener {
                        SportashAPI.pickImageFromCamera(this@EditUserFragment)
                    }
                    findViewById<Button>(R.id.edit_user_remove_image).setOnClickListener {
                        removeImage()
                    }
                    findViewById<Button>(R.id.edit_user_submit).setOnClickListener {
                        submitChanges()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val container = view?.findViewById<ImageView>(R.id.edit_user_image)
        if(requestCode == SportashAPI.PICK_IMAGE && resultCode == Activity.RESULT_OK){
            imageExists = true
            val imageUri = data?.data
            container?.setImageURI(imageUri)
        } else if(requestCode == SportashAPI.SHOOT_IMAGE && resultCode == Activity.RESULT_OK){
            imageExists = true
            var photo = data?.extras?.get("data") as Bitmap
            container?.setImageBitmap(photo)
        }
    }

    private fun removeImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)
            SportashAPI.HTTPRequest(SportashAPI.DELETE, "${SportashAPI.apiURL}/users/delete-image?id=$userID", null)
            withContext(Dispatchers.Main){
                populateUser()
                imageExists = false
            }
        }

    }

    private fun submitChanges() {
        var userImage: String? = null
        if(imageExists) {
            userImage = SportashAPI.convertImageToBase64(view?.findViewById(R.id.edit_user_image)!!)
        }
        if(view?.findViewById<EditText>(R.id.edit_user_username)?.text.toString().isEmpty()){
            Toast.makeText(context, "Username must not be empty", Toast.LENGTH_SHORT).show()
            return
        }

        var dob: String? = null
        if(user?.DOB != "null") {
            dob = user?.DOB!!
        }
        val data = FullUserDetails(
            user?.ID!!,
            userImage,
            view?.findViewById<EditText>(R.id.edit_user_username)?.text.toString(),
            user?.Login_Email!!,
            user?.Login_Password!!,
            view?.findViewById<EditText>(R.id.edit_user_firstname)?.text.toString(),
            view?.findViewById<EditText>(R.id.edit_user_lastname)?.text.toString(),
            dob
        )

        CoroutineScope(Dispatchers.IO).launch {
            val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)

            SportashAPI.HTTPRequest(SportashAPI.PUT, "${SportashAPI.apiURL}/users?id=$userID", data)
            activity?.supportFragmentManager?.popBackStack()
        }
    }
}