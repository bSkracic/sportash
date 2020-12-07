package com.example.sportash

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

// Data model used for representing user details in application
class BaseUserDetails(jsonObject: JSONObject) {
    var id: Int = 0
    var username: String = ""
    var image: String? = null
    var befriended: Boolean? = null

    init {
        id = jsonObject.getInt("ID")
        username = jsonObject.getString("Username")
        image = null
    }
}

// Data model used for communication with the api
data class FullUserDetails (
    val ID: Int,
    val Username: String,
    val Login_Email: String,
    val Login_Password: String,
    val FirstName: String,
    val LastName: String,
    val DOB: String )

