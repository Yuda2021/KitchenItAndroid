package com.example.julyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var productsTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var userEmailTextView: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var addCommentButton: Button
    private lateinit var commentEditText: EditText

    private var comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        titleTextView = findViewById(R.id.detailTitle)
        contentTextView = findViewById(R.id.detailContent)
        productsTextView = findViewById(R.id.detailProducts)
        imageView = findViewById(R.id.detailImage)
        userEmailTextView = findViewById(R.id.detailUserEmail)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        addCommentButton = findViewById(R.id.addCommentButton)
        commentEditText = findViewById(R.id.commentEditText)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val products = intent.getStringExtra("products") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        comments = intent.getParcelableArrayListExtra<Comment>("comments")?.toMutableList() ?: mutableListOf()

        titleTextView.text = title
        contentTextView.text = content
        productsTextView.text = products
        userEmailTextView.text = "Posted by: $userEmail"
        Glide.with(this).load(imageUrl).into(imageView)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(comments)
        commentsRecyclerView.adapter = commentAdapter

        addCommentButton.setOnClickListener {
            val newCommentContent = commentEditText.text.toString()
            if (newCommentContent.isNotEmpty()) {
                val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                val currentUserEmail = prefs.getString("email", "unknown")
                val newComment = Comment(userEmail = currentUserEmail ?: "unknown", content = newCommentContent)
                comments.add(newComment)
                commentAdapter.notifyDataSetChanged()
                commentEditText.text.clear()
            }
        }
    }
}
