package com.example.KitchenIt.viewModel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.view.RecipeDetailActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.InputStream

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var editProducts: EditText
    private lateinit var buttonUpdateRecipe: Button
    private lateinit var buttonSelectImage: Button

    private var imageUri: Uri? = null
    private var originalTitle: String? = null
    //private var originalImageUrl: String? = null
    private var userEmail: String? = null

    private val IMAGE_PICK_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Initialize Firestore and Storage
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        imageView = findViewById(R.id.editRecipeImage)

        // Get the current recipe details from the intent
        originalTitle = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val products = intent.getStringExtra("products")
        userEmail = intent.getStringExtra("userEmail")
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""

        // Initialize views
        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        editProducts = findViewById(R.id.editProducts)
        buttonUpdateRecipe = findViewById(R.id.buttonUpdateRecipe)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        progressBar = findViewById(R.id.progressBar)

        // Set current recipe details to views
        editTitle.setText(originalTitle)
        editContent.setText(content)
        editProducts.setText(products)

        // Load the current image
        Glide.with(this).load(imageUrl).into(imageView)


        // Set click listener for the select image button
        buttonSelectImage.setOnClickListener {
            openImagePicker()
        }

        // Set click listener for update button
        buttonUpdateRecipe.setOnClickListener {
            val newTitle = editTitle.text.toString()
            val newContent = editContent.text.toString()
            val newProducts = editProducts.text.toString()

            updateRecipeInDb(originalTitle, newTitle, newContent, newProducts)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                // Display the selected image
                imageView.setImageURI(it)
            }
        }
    }

    private fun updateRecipeInDb(originalTitle: String?, newTitle: String, newContent: String, newProducts: String) {
        if (originalTitle == null || newTitle.isEmpty() || newContent.isEmpty() || newProducts.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE
        //showLoading()

        val updatedRecipe = hashMapOf(
            "title" to newTitle,
            "content" to newContent,
            "products" to newProducts
        )

        if (imageUri != null) {
            // Upload the new image
            val fileRef = storageRef.child("recipe_images/${imageUri?.lastPathSegment}")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        updatedRecipe["imageUrl"] = uri.toString()
                        updateRecipeInFirestore(originalTitle, updatedRecipe)
                    }
                }
                .addOnFailureListener { e ->
                    // Hide progress bar
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No image selected, just update other fields
            updateRecipeInFirestore(originalTitle, updatedRecipe)
        }
    }

    private fun updateRecipeInFirestore(originalTitle: String?, updatedRecipe: Map<String, Any>) {
        db.collection("recipes")
            .whereEqualTo("title", originalTitle)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.update(updatedRecipe)
                        .addOnSuccessListener {
                            // Hide progress bar
                            //hideLoading()
                            progressBar.visibility = View.GONE

                            Toast.makeText(this, "Recipe updated successfully", Toast.LENGTH_SHORT).show()
                            // Refresh the recipe detail activity
                            val intent = Intent(this, RecipeDetailActivity::class.java)
                            intent.putExtra("title", updatedRecipe["title"] as String)
                            intent.putExtra("content", updatedRecipe["content"] as String)
                            intent.putExtra("products", updatedRecipe["products"] as String)
                            intent.putExtra("imageUrl", updatedRecipe["imageUrl"] as String?)
                            intent.putExtra("userEmail", userEmail)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                            startActivity(intent)
                            finish()  // Close the current activity
                        }
                        .addOnFailureListener { e ->
                            // Hide progress bar
                            progressBar.visibility = View.GONE

                            Toast.makeText(this, "Error updating recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Hide progress bar
                progressBar.visibility = View.GONE

                Toast.makeText(this, "Error fetching recipe for update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun showLoading() {
        findViewById<View>(R.id.overlay).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    // Hide overlay and spinner
    fun hideLoading() {
        findViewById<View>(R.id.overlay).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }
}
