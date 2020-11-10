package com.example.sportash

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, HomeFragment()).commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Retrieve User ID
        val id = getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0)
        if(id == 0){
            Toast.makeText(this, R.string.default_error_message, Toast.LENGTH_SHORT).show()
            return
        }

        // Hide action bar, making the display semi-fullscreen
        this.supportActionBar?.hide()

        // Setup fragment container with home fragment as default replaceable fragment
        supportFragmentManager.beginTransaction().add(R.id.main_fragment_container, HomeFragment())
                .commit()

        // Define navigation
        findViewById<TabLayout>(R.id.tabLayout).addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        when (findViewById<TabLayout>(R.id.tabLayout).selectedTabPosition) {
                            0 -> savedInstanceState
                                    ?: supportFragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment_container, HomeFragment()).commit()
                            2 -> savedInstanceState
                                ?: supportFragmentManager.beginTransaction()
                                    .replace(R.id.main_fragment_container, MessagesFragment()).commit()
                            1 -> savedInstanceState
                                    ?: supportFragmentManager.beginTransaction()
                                        .replace(R.id.main_fragment_container, FriendsFragment()).commit()
                            3 -> savedInstanceState
                                    ?: supportFragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment_container, UserFragment()).commit()
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                    }
                }
        )
    }
}