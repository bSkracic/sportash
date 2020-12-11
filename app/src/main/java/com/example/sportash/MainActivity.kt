package com.example.sportash

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val stackSize = supportFragmentManager.backStackEntryCount
        if(stackSize > 0) {
            supportFragmentManager.popBackStack()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, HomeFragment()).commit()
        }
        findViewById<TabLayout>(R.id.tabLayout).visibility = View.VISIBLE
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
                            3 -> {
                                val uID = getSharedPreferences("sportash", Context.MODE_PRIVATE)?.getInt("USER_ID", 0) as Int
                                val fragment = UserFragment.newInstance(uID, true)
                                savedInstanceState
                                    ?: supportFragmentManager.beginTransaction()
                                        .replace(R.id.main_fragment_container, fragment).commit()

                            }
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