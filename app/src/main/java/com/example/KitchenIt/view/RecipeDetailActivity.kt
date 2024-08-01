package com.example.KitchenIt.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.KitchenIt.R

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var productsTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var userEmailTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        titleTextView = findViewById(R.id.detailTitle)
        contentTextView = findViewById(R.id.detailContent)
        productsTextView = findViewById(R.id.detailProducts)
        imageView = findViewById(R.id.detailImage)
        userEmailTextView = findViewById(R.id.detailUserEmail)


        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val products = intent.getStringExtra("products") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""

        titleTextView.text = title
        contentTextView.text = content
        productsTextView.text = products
        userEmailTextView.text = "Posted by: $userEmail"
        Glide.with(this).load(imageUrl).into(imageView)



    }
}
