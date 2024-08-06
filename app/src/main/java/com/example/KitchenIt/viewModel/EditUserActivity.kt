package com.example.KitchenIt.viewModel

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditUserActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imageViewProfile: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonChangePassword: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
        progressBar = findViewById(R.id.progressBar)
        imageViewProfile = findViewById(R.id.imageViewProfile)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonChangePassword = findViewById(R.id.buttonChangePassword)

        // Display user email at the top
        val userEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("email", "")
        editTextEmail.setText(userEmail)

        // Load existing user data
        loadUserData()

        buttonSelectImage.setOnClickListener {
            openImagePicker()
        }

        buttonSave.setOnClickListener {
            saveUserChanges()
        }

        buttonCancel.setOnClickListener {
            finish()  // Close the activity
        }

        buttonChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserData() {
        val userEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("email", "")

        db.collection("users").document(userEmail!!).get().addOnSuccessListener { document ->
            if (document != null) {
                editTextName.setText(document.getString("name"))
                // Load profile image
                val profileImageUrl = document.getString("imageUrl")
                if (profileImageUrl != null) {
                    Glide.with(this).load(profileImageUrl).into(imageViewProfile)
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageViewProfile.setImageURI(selectedImageUri)
        }
    }

    private fun saveUserChanges() {
        val name = editTextName.text.toString()

        if (validateInputs(name)) {
            progressBar.visibility = View.VISIBLE

            val userEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("email", "")

            if (selectedImageUri != null) {
                uploadProfileImage(userEmail!!)
            } else {
                updateUserDetails(userEmail!!)
            }
        }
    }

    private fun validateInputs(name: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadProfileImage(userEmail: String) {
        selectedImageUri?.let { uri ->
            val storageRef = storage.child("profile_images/${userEmail}.jpg")
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val imageUrl = downloadUrl.toString()
                    updateUserDetails(userEmail, imageUrl)
                }.addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error getting image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            updateUserDetails(userEmail)
        }
    }

    private fun updateUserDetails(userEmail: String, profileImageUrl: String? = null) {
        val updates = hashMapOf<String, Any>(
            "name" to editTextName.text.toString()
        )

        profileImageUrl?.let {
            updates["imageUrl"] = it
        }

        db.collection("users").document(userEmail).update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "User details updated successfully", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error updating user details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val currentPassword = dialogView.findViewById<EditText>(R.id.editTextCurrentPassword)
        val newPassword = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPassword = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)
        val buttonChange = dialogView.findViewById<Button>(R.id.buttonChangePassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            buttonChange.setOnClickListener {
                val currentPass = currentPassword.text.toString()
                val newPass = newPassword.text.toString()
                val confirmPass = confirmPassword.text.toString()

                if (newPass != confirmPass) {
                    Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (currentPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val userEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("email", "")

                if (userEmail != null) {
                    // Check current password and update if correct
                    db.collection("users").document(userEmail).get().addOnSuccessListener { document ->
                        val storedPassword = document.getString("password")  // Assuming passwords are stored in Firestore

                        if (storedPassword == currentPass) {
                            // Passwords match, proceed with updating the password
                            db.collection("users").document(userEmail).update("password", newPass)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error checking current password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

}

