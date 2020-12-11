package com.example.sportash

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    // Data class model required because gson cannot serialize anonymous objects
    data class Request(val Login_Email: String, val Login_Password: String)

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
        // needs better
        findViewById<Button>(R.id.btn_login_submit).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_logo).visibility = View.VISIBLE
        findViewById<EditText>(R.id.edit_email).visibility = View.VISIBLE
        findViewById<EditText>(R.id.edit_password).visibility = View.VISIBLE
        findViewById<Switch>(R.id.switch_remember).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txt_new_acc).visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Remove action bar title
        this.title = ""

        // Check if user ID is store: if any skip this activity and open MainActivity
        if(getSharedPreferences("sportash", Context.MODE_PRIVATE).getBoolean(SportashAPI.USER_STORED, false)){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Very bad part of code; remove elements from activity when fragment is displayed: prevent overlap
        findViewById<TextView>(R.id.txt_new_acc).setOnClickListener{
            findViewById<Button>(R.id.btn_login_submit).visibility = View.GONE
            findViewById<TextView>(R.id.txt_logo).visibility = View.GONE
            findViewById<EditText>(R.id.edit_email).visibility = View.GONE
            findViewById<EditText>(R.id.edit_password).visibility = View.GONE
            findViewById<Switch>(R.id.switch_remember).visibility = View.GONE
            findViewById<TextView>(R.id.txt_new_acc).visibility = View.GONE
            supportFragmentManager.beginTransaction().replace(R.id.new_acc_fragment_container, NewAccountFragment()).addToBackStack("NEW_USER").commit()
        }

        // Questionable passing reference to switch; should be accessed with "view" from method
        findViewById<Button>(R.id.btn_login_submit).setOnClickListener{
            val email = findViewById<EditText>(R.id.edit_email).text.toString()
            val password = findViewById<EditText>(R.id.edit_password).text.toString()
            val request = Request(email, password)
            checkLogin(request)
        }
    }

    private fun checkLogin(request: Any){
        CoroutineScope(Dispatchers.IO).launch {
            var response = ""
            try{
                response = SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/users/login", request)
            } catch (ex: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
            finally {
                if(response != "") {
                    if (JSONObject(response).getInt("id") != -1){
                        val id =  JSONObject(response).getInt("id")
                        val sharedPreferences = getSharedPreferences("sportash", Context.MODE_PRIVATE)
                        if(findViewById<Switch>(R.id.switch_remember).isChecked){
                            sharedPreferences.edit().putBoolean("USER_STORED", true).apply()
                        }
                        sharedPreferences.edit().putInt("USER_ID", id).apply()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        withContext(Dispatchers.Main){
                            findViewById<TextView>(R.id.txt_login_error)?.text = getString(R.string.login_failed)
                        }
                    }
                }
            }
        }
    }
}