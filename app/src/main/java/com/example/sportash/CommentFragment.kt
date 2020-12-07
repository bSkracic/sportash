package com.example.sportash

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val POST_ID = "POST_ID"

class CommentFragment : Fragment(R.layout.fragment_comment) {
    private var postID: Int = 0
    private var post: Post? = null
    private var commentList: MutableList<Message> = mutableListOf()

    companion object {
        @JvmStatic
        fun newInstance(postID: Int) =
            CommentFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID, postID)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postID = it.getInt(POST_ID)
        }
    }

    data class CommentRequest(var UserID: Int, var PostID: Int, var Content: String)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<EditText>(R.id.comments_new_comment).setText("")

        super.onViewCreated(view, savedInstanceState)
        populatePost()
        populateComments()

        view.findViewById<Button>(R.id.post_report).setOnClickListener{
            // TODO: Send report request
        }

        view.findViewById<Button>(R.id.comments_send).setOnClickListener{
            val body = CommentRequest(
                activity?.getSharedPreferences(SportashAPI.PREF_KEY, Context.MODE_PRIVATE)?.getInt(SportashAPI.USER_ID, 0)!!,
                postID,
                view.findViewById<EditText>(R.id.comments_new_comment).text.toString()
            )
            CoroutineScope(Dispatchers.IO).launch {
                SportashAPI.HTTPRequest(SportashAPI.POST, "${SportashAPI.apiURL}/Comments", body)
                withContext(Dispatchers.Main){
                    populateComments()
                }
            }
        }
    }

    private fun populatePost() {
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/PostEndorsments/$postID", null).apply {
                post = Post(JSONObject(this))
            }
            withContext(Dispatchers.Main){
                view?.apply {
                    findViewById<TextView>(R.id.comments_username).text = post?.Username
                    findViewById<TextView>(R.id.comments_post).text = post?.Content
                    findViewById<TextView>(R.id.comments_tag).text = post?.Tags
                }
            }
        }

    }


    private fun populateComments() {
        commentList = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            SportashAPI.HTTPRequest(SportashAPI.GET, "${SportashAPI.apiURL}/Comments/$postID", null).apply {
                val results = JSONArray(this)
                for(i in 0 until results.length()){
                    commentList.add(Message(results.getJSONObject(i)))
                }
            }
            withContext(Dispatchers.Main){
                view?.findViewById<RecyclerView>(R.id.comments_container)?.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = DetailedMessageAdapter(commentList)
                }
            }
        }
    }
}