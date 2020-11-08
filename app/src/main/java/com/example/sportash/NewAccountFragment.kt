package com.example.sportash

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.*

class NewAccountFragment : Fragment(R.layout.fragment_new_account) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            view.findViewById<Button>(R.id.btn_acc_submit).setOnClickListener {
                createNewUser(view)
            }
    }

    private fun createNewUser(view: View) {
        var data = FullUserDetails (
            0,
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

        val job = CoroutineScope(Dispatchers.IO).launch{
            val url = "${SportashAPI.apiURL}/users/new-user"
            SportashAPI.HTTPRequest(SportashAPI.POST, url, data)
        }
        // Wait for the job to finish: display dialog afterwards
        runBlocking {
            job.join()
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