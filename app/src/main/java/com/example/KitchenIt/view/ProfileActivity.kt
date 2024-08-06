package com.example.KitchenIt.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var editTextName: EditText
    private lateinit var buttonUpdateProfile: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        editTextName = findViewById(R.id.editTextName)
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        loadUserProfile()

        buttonSelectImage.setOnClickListener {
            openImagePicker()
        }

        buttonUpdateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadUserProfile() {
        val user = firebaseAuth.currentUser
        user?.let {
            val email = it.email
            if (email != null) {
                firestore.collection("users").document(email).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val name = document.getString("name")
                            val imageUrl = document.getString("imageUri")
                            Log.d("ProfileActivity", "Fetched user data: name = $name, imageUri = $imageUrl")
                            editTextName.setText(name)
                            if (imageUrl != null && imageUrl.isNotEmpty()) {
                                Glide.with(this).load(imageUrl).into(profileImageView)
                            } else {
                                profileImageView.setImageResource(R.drawable.avatar)
                            }
                        } else {
                            Log.d("ProfileActivity", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Failed to load profile: ${e.message}", e)
                        Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("ProfileActivity", "User email is null")
            }
        } ?: Log.d("ProfileActivity", "No user is logged in")
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun updateProfile() {
        val name = editTextName.text.toString().trim()
        if (name.isEmpty()) {
            editTextName.error = "Name is required"
            editTextName.requestFocus()
            return
        }

        val user = firebaseAuth.currentUser
        user?.let {
            val email = it.email
            if (email != null) {
                val userProfile = hashMapOf<String, Any>(
                    "name" to name,
                    "email" to email
                )

                if (imageUri != null) {
                    val storageRef = firebaseStorage.reference.child("profile_images/$email")
                    storageRef.putFile(imageUri!!)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                userProfile["imageUri"] = uri.toString()
                                saveUserProfile(email, userProfile)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileActivity", "Failed to upload image: ${e.message}", e)
                            Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    saveUserProfile(email, userProfile)
                }
            } else {
                Log.d("ProfileActivity", "User email is null")
            }
        } ?: Log.d("ProfileActivity", "No user is logged in")
    }

    private fun saveUserProfile(email: String, userProfile: Map<String, Any>) {
        firestore.collection("users").document(email).set(userProfile)
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Profile updated successfully")
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Failed to update profile: ${e.message}", e)
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
