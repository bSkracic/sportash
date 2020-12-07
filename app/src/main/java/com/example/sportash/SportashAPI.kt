package com.example.sportash

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

abstract class SportashAPI {
    companion object {
        const val GET: String = "GET"
        const val POST: String = "POST"
        const val PUT: String = "PUT"
        const val DELETE: String = "DELETE"

        const val apiURL = "https://hk-iot-team-02.azurewebsites.net/api"

        const val PREF_KEY = "sportash"
        const val USER_ID = "USER_ID"

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