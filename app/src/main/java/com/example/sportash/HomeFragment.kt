package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray


class HomeFragment : Fragment(R.layout.fragment_home) {
    var posts: MutableList<Post> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populatePosts()
    }

    private fun populatePosts() {
        posts = mutableListOf()
        val userID = activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/Posts/$userID", null).apply {
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    posts.add(Post(results.getJSONObject(i)))
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
                        })
                    }
                }
            }
        }
    }

}