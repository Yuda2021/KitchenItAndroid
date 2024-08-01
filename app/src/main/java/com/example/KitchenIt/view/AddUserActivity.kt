package com.example.KitchenIt.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.KitchenIt.R
import com.google.firebase.firestore.FirebaseFirestore

class AddUserActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSubmit: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonGoToLogin: Button
    private lateinit var progressBar: ProgressBar

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin)
        progressBar = findViewById(R.id.progressBar)
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)//"user_prefs"


        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "imageUri" to (imageUri?.toString() ?: "")
            )
            showProgressBar()
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        db.collection("users").add(user)
                            .addOnSuccessListener {
                                saveUserDetailsLocally(name, email, password, imageUri?.toString() ?: "")
                                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                                hideProgressBar()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                hideProgressBar()
                            }
                    } else {
                        Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                }
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageUri?.let {
                imageView.setImageURI(it)
            }
        }
    }

    private fun saveUserDetailsLocally(name: String, email: String, password: String, imageUri: String) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putString("imageUri", imageUri)
        editor.apply()

    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }
}
