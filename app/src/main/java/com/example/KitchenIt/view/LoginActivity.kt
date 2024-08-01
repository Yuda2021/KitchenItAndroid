package com.example.KitchenIt.view


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.KitchenIt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoToRegister: Button
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "User not found, please register first", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = documents.documents[0]
                        if (user.getString("password") == password) {
                            // Save user details locally
                            getSharedPreferences("UserSession", MODE_PRIVATE).edit().apply {
                                putString("email", user.getString("email"))
                                putString("name", user.getString("name"))
                                putString("imageUri", user.getString("imageUri"))
                                apply()
                            }
                            auth = FirebaseAuth.getInstance()
                            auth.signInWithEmailAndPassword(email, password)

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        buttonGoToRegister.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
