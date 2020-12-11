package com.example.sportash

import org.json.JSONObject

// Data model used for representing user details in application
class BaseUserDetails(jsonObject: JSONObject) {
    var id: Int = 0
    var username: String = ""
    var image: String? = null
    var befriended: Boolean? = null

    init {
        id = jsonObject.getInt("ID")
        username = jsonObject.getString("Username")
        image = jsonObject.getString("Image")
    }
}

// Data model used for communication with the api
data class FullUserDetails (
    val ID: Int,
    val Image: String?,
    val Username: String,
    val Login_Email: String,
    val Login_Password: String,
    val FirstName: String,
    val LastName: String,
    val DOB: String?
    )

