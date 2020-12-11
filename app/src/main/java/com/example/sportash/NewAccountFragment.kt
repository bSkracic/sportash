package com.example.sportash

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import kotlinx.coroutines.*

class NewAccountFragment : Fragment(R.layout.fragment_new_account) {
    var imageExists = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_acc_submit).setOnClickListener {
            createNewUser(view)
        }
        view.findViewById<Button>(R.id.edit_new_image_pick).setOnClickListener {
            SportashAPI.pickImageFromGallery(this@NewAccountFragment)
        }
        view.findViewById<Button>(R.id.edit_new_image_take).setOnClickListener {
            SportashAPI.pickImageFromCamera(this@NewAccountFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val container = view?.findViewById<ImageView>(R.id.edit_new_image)
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

    private fun createNewUser(view: View) {

        var image: String? = null
        if(imageExists) {
            image = SportashAPI.convertImageToBase64(view?.findViewById(R.id.edit_new_image))
        }

        var data = FullUserDetails (
            0,
            image,
            view.findViewById<EditText>(R.id.edit_new_username).text.toString(),
            view.findViewById<EditText>(R.id.edit_new_email).text.toString(),
            view.findViewById<EditText>(R.id.edit_new_password).text.toString(),
            view.findViewById<EditText>(R.id.edit_new_firstname).text.toString(),
            view.findViewById<EditText>(R.id.edit_new_lastname).text.toString(),
            view.findViewById<EditText>(R.id.edit_new_dob).text.toString()
        )
        if(data.Username.isEmpty() || data.Login_Email.isEmpty() || data.Login_Password.isEmpty()){
            Toast.makeText(context, "Username, Email and Password are required!", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.IO).launch{
            val url = "${SportashAPI.apiURL}/users/new-user"
            SportashAPI.HTTPRequest(SportashAPI.POST, url, data)
            withContext(Dispatchers.Main){
                AlertDialog.Builder(context).run{
                    setTitle("SPORTASH")
                    setMessage("Account successfully created!")
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(context, LoginActivity::class.java)
                            startActivity(intent)
                            activity?.supportFragmentManager?.beginTransaction()?.remove(this@NewAccountFragment)?.commit()
                        }
                    show()
                }
            }
        }

    }
}