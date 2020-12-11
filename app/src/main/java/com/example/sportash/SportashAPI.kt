package com.example.sportash

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
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
        const val USER_STORED = "USER_STORED"

        const val PICK_IMAGE = 1
        const val SHOOT_IMAGE = 2

        fun pickImageFromGallery(fragment: Fragment) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            fragment.startActivityForResult(intent, PICK_IMAGE)
        }

        fun pickImageFromCamera(fragment: Fragment) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            fragment.startActivityForResult(intent, SHOOT_IMAGE)
        }

        fun setImage(base64Image: String?, container: ImageView) {
            try{
                val imageByte = Base64.decode(base64Image, Base64.NO_WRAP)
                val bmp: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
                container.setImageBitmap(bmp)
            }catch(ex: Exception){
                container.setImageResource(R.mipmap.default_avatar_foreground)
            }
        }

        fun convertImageToBase64(container: ImageView): String? {
            val bitmap = container.drawable.toBitmap()
            return if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } else {
                null
            }
        }

        fun HTTPRequest(method: String, url: String, data: Any?): String {
            // Create JSON body if any is given
            var jsonContent = ""
            if(data != null){
                val gson = GsonBuilder().serializeNulls().create()
                jsonContent = gson.toJson(data)
            }
            // Send HTTP request
            try {
                with(URL(url).openConnection() as HttpURLConnection) {
                    this.requestMethod = method
                    if(method == POST || method == PUT){
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