package com.example.sportash

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class NewPostFragment : Fragment(R.layout.fragment_new_post) {
    var tags: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)

        view.findViewById<Button>(R.id.new_post_tag_submit).setOnClickListener {
            val tag = view.findViewById<EditText>(R.id.new_post_new_tag).text.toString()
            if(tag.isNotEmpty()) {
                tags.add(tag)
                loadTags()
            }
            else {
                Toast.makeText(context, "Tag cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.new_post_submit).setOnClickListener{
            val content = view.findViewById<EditText>(R.id.new_post_content).text.toString()
            if(content.length > 140) {
                Toast.makeText(context, "Post length must be maximum 140", Toast.LENGTH_SHORT).show()

            }
            else {
                var post = Post()
                post.UserID = userID!!
                post.P_Timestamp = LocalDateTime.now().toString()
                post.Content = content
                var tagText = ""
                if(tags.size != 0) {
                    tagText = tags[0]
                    for(i in 1 until tags.size){
                        tagText += ",${tags[i]}"
                    }
                }
                post.Tags = tagText
                sendPost(post)
            }
        }
    }

    private fun loadTags() {
        view?.findViewById<RecyclerView>(R.id.new_post_tags_container)?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TagAdapter(tags, object: OnTagClicked {
                override fun onRemoveClicked(index: Int) {
                    tags.removeAt(index)
                    loadTags()
                }
            })
        }
    }

    private fun sendPost(post: Post) {
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/Posts", post)
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.apply {
                    popBackStack()
                    beginTransaction().replace(R.id.main_fragment_container, HomeFragment()).commit()
                }
            }
        }
    }
}