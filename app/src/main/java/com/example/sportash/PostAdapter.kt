package com.example.sportash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.util.*

class Post {
    var ID: Int = 0
    var UserID: Int = 0
    var Username: String = ""
    var Content: String = ""
    var Tags: String = ""
    var P_Timestamp: String = ""
    var Endorsed : Boolean = false
    var EndorsementCount: Int = 0
    var CommentCount: Int = 0
    constructor(data: JSONObject) {
        ID = data.getInt("ID")
        UserID = data.getInt("UserID")
        Username = data.getString("Username")
        Content = data.getString("Content")
        Tags = data.getString("Tags")
        P_Timestamp = data.getString("P_Timestamp")
        Endorsed = data.getBoolean("Endorsed")
        EndorsementCount = data.getInt("EndorsementCount")
        CommentCount = data.getInt("CommentCount")
    }
    constructor(){}
}

// TODO: add image to each poster in posts

class PostItem(var itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var pContent : TextView? = null
    private var pUsername : TextView? = null
    private var pDate : TextView? = null
    private var pEndorse : ImageView? = null
    private var pComment: ImageView? = null
    private var pEndorsementCount: TextView? = null
    private var pCommentCount: TextView? = null
    private var pUserImage: ImageView? = null

    init {
        pContent = itemView.findViewById(R.id.post_content)
        pUsername = itemView.findViewById(R.id.post_user)
        pEndorse = itemView.findViewById(R.id.post_endorsement)
        pComment = itemView.findViewById(R.id.post_comments)
        pDate = itemView.findViewById(R.id.post_date)
        pEndorsementCount = itemView.findViewById(R.id.post_endorsement_count)
        pCommentCount = itemView.findViewById(R.id.post_comments_count)
        pUserImage = itemView.findViewById(R.id.post_user_image)
    }

    fun bind(post: Post, userImage: String?, clickListener: OnPostClicked){
        pUsername?.text = post.Username
        pContent?.text = post.Content
        pCommentCount?.text = post.CommentCount.toString()
        pEndorsementCount?.text = post.EndorsementCount.toString()
        pEndorse?.setOnClickListener {
            clickListener.onEndorsed(post)
            if(post.Endorsed) pEndorse?.setImageResource(R.mipmap.like_empty_foreground)
            else pEndorse?.setImageResource(R.mipmap.like_full_foreground)
        }
        pComment?.setOnClickListener {
            clickListener.onCommentsClicked(post)
        }
        val dateTime = post.P_Timestamp.split('T')
        val timestamp = "${dateTime[0]} ${dateTime[1]}"
        pDate?.text = timestamp
        if(post.Endorsed) pEndorse?.setImageResource(R.mipmap.like_full_foreground)
        else pEndorse?.setImageResource(R.mipmap.like_empty_foreground)

        SportashAPI.setImage(userImage, pUserImage!!)

        itemView.setOnClickListener{
            clickListener.onCommentsClicked(post)
        }
    }
}

class PostAdapter(private val postList: MutableList<Post>, private val clickListener: OnPostClicked, private val images: MutableMap<Int, String?>) : RecyclerView.Adapter<PostItem>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostItem(view)
    }

    override fun onBindViewHolder(holder: PostItem, position: Int) {
        val post = postList[position]
        val userImage = images[post.UserID]
        holder.bind(post, userImage, clickListener)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}

interface OnPostClicked {
    fun onEndorsed(post: Post)
    fun onCommentsClicked(post: Post)
}
