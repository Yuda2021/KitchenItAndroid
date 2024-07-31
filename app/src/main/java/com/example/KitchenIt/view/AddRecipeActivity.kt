package com.example.KitchenIt.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.KitchenIt.viewModel.FirebaseStorageUtils
import com.example.KitchenIt.R
import com.google.firebase.firestore.FirebaseFirestore


class AddRecipeActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var editTextProducts: EditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        editTextProducts = findViewById(R.id.editTextProducts)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
        progressBar = findViewById(R.id.progressBar)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString()
            val content = editTextContent.text.toString()
            val products = editTextProducts.text.toString()
            val userEmail = getUserEmail(this)

            if (title.isNotEmpty() && content.isNotEmpty() && products.isNotEmpty() && imageUri != null) {
                progressBar.visibility = View.VISIBLE
                buttonSave.isEnabled = false
                buttonCancel.isEnabled = false
                FirebaseStorageUtils.uploadFile(imageUri!!, { imageUrl ->
                    saveRecipeToFirestore(title, content, imageUrl, products, userEmail)
                }, { error ->
                    progressBar.visibility = View.GONE
                    buttonSave.isEnabled = true
                    buttonCancel.isEnabled = true
                    Toast.makeText(
                        this,
                        "Error uploading file: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            } else {
                Toast.makeText(
                    this,
                    "Please fill out all fields and select an image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun saveRecipeToFirestore(title: String, content: String, imageUrl: String, products: String, userEmail: String?) {
        val db = FirebaseFirestore.getInstance()
        val recipe = hashMapOf(
            "title" to title,
            "content" to content,
            "imageUrl" to imageUrl,
            "products" to products,
            "userEmail" to userEmail,  // Use retrieved email
            "timestamp" to System.currentTimeMillis()  // Add timestamp
        )

        db.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                buttonSave.isEnabled = true
                buttonCancel.isEnabled = true
                Toast.makeText(this, "Recipe saved", Toast.LENGTH_SHORT).show()

                // Restart the main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                buttonSave.isEnabled = true
                buttonCancel.isEnabled = true
                Toast.makeText(this, "Error saving recipe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserEmail(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email", null)
    }
}
