package com.example.KitchenIt.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.viewModel.EditRecipeActivity

import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var productsTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var userEmailTextView: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        titleTextView = findViewById(R.id.detailTitle)
        contentTextView = findViewById(R.id.detailContent)
        productsTextView = findViewById(R.id.detailProducts)
        imageView = findViewById(R.id.detailImage)
        userEmailTextView = findViewById(R.id.detailUserEmail)
        progressBar = findViewById(R.id.progressBar)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val products = intent.getStringExtra("products") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        val buttonEditRecipe = findViewById<Button>(R.id.buttonEditRecipe)
        val buttonDeleteRecipe = findViewById<Button>(R.id.buttonDeleteRecipe)


        titleTextView.text = title
        contentTextView.text = content
        productsTextView.text = products
        userEmailTextView.text = "Posted by: $userEmail"
        Glide.with(this).load(imageUrl).into(imageView)

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        if (sharedPreferences.getString("email", null) == userEmail) {
            buttonEditRecipe.visibility = View.VISIBLE
            buttonDeleteRecipe.visibility = View.VISIBLE
        }
        buttonEditRecipe.setOnClickListener {
                 val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra("title", title)
                putExtra("content", content)
                putExtra("products", products)
                putExtra("userEmail", userEmail)
                putExtra("imageUrl",imageUrl)
            }
            startActivity(intent)
        }

        buttonDeleteRecipe.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Delete Recipe")
            alertDialog.setMessage("Are you sure you want to delete this recipe?")
            alertDialog.setPositiveButton("Yes") { _, _ ->
                deleteRecipeFromDb(title)
            }
            alertDialog.setNegativeButton("No", null)
            alertDialog.show()
        }

    }




    private fun deleteRecipeFromDb(title: String?) {
        if (title == null) return
        progressBar.visibility = View.VISIBLE
        db.collection("recipes")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                        .addOnSuccessListener {  // Hide progress bar
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                            // Refresh the main activity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()  // Close the current activity
                        }
                        .addOnFailureListener { e ->
                            // Hide progress bar
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Error deleting recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Hide progress bar
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching recipe for deletion: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
