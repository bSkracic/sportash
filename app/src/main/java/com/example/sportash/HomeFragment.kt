package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class HomeFragment : Fragment(R.layout.fragment_home) {
    var posts: MutableList<Post> = mutableListOf()
    var images: MutableMap<Int, String?> = mutableMapOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populatePosts()
        view.findViewById<FloatingActionButton>(R.id.fab_new_post).setOnClickListener {
            activity?.supportFragmentManager?.apply {
                beginTransaction().replace(R.id.main_fragment_container, NewPostFragment()).addToBackStack("NEW_POST").commit()
            }
        }
    }

    private fun populatePosts() {
        posts = mutableListOf()
        images = mutableMapOf()
        val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/Posts/$userID", null).apply {
                val results = JSONObject(this)
                val jsonPosts = results.getJSONArray("Posts")
                val jsonImages = results.getJSONArray("Images")
                for(i in 0 until jsonPosts.length()) {
                    posts.add(Post(jsonPosts.getJSONObject(i)))
                }
                for(i in 0 until jsonImages.length()) {
                    val entry = jsonImages.getJSONObject(i)
                    images.put(entry.getInt("userID"), entry.getString("Image"))
                }
                withContext(Dispatchers.Main){
                    view?.findViewById<RecyclerView>(R.id.home_posts_container)?.apply {
                        this.layoutManager = LinearLayoutManager(context)
                        this.adapter = PostAdapter(posts, object : OnPostClicked {
                            override fun onEndorsed(post: Post) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/PostEndorsments?userID=$userID&postID=${post.ID}", null)
                                    withContext(Dispatchers.Main){
                                        populatePosts()
                                    }
                                }
                            }
                            override fun onCommentsClicked(post: Post) {
                                activity?.supportFragmentManager?.apply {
                                    beginTransaction().replace(R.id.main_fragment_container, CommentFragment.newInstance(post.ID)).addToBackStack("COMMENTS").commit()
                                }
                            }
                        }, images)
                    }
                }
            }
        }
    }

}