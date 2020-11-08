package com.example.sportash

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

class SportashAPI() {
    companion object {
        const val GET: String = "GET"
        const val POST: String = "POST"
        const val PUT: String = "PUT"
        const val DELETE: String = "DELETE"

        const val apiURL = "https://hk-iot-team-02.azurewebsites.net/api"

        fun HTTPRequest(method: String, url: String, data: Any?): String {
            // Create JSON body if any is given
            var jsonContent = ""
            if(data != null){
                jsonContent = Gson().toJson(data)
            }
            // Send HTTP request
            try {
                with(URL(url).openConnection() as HttpURLConnection) {
                    this.requestMethod = method
                    if(method == "POST" || method == "PUT"){
                        this.setRequestProperty("content-type", "application/json")
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(jsonContent)
                        wr.flush()
                    }

                    BufferedReader(InputStreamReader(inputStream)).use {
                        val response = StringBuffer()
                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        return response.toString()
                    }
                }
            } catch (ex: Exception){
                throw ex
            }
        }
    }
}

