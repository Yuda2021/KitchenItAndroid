// MainActivity.kt
package com.example.KitchenIt.view


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.KitchenIt.R
import com.example.KitchenIt.Recipe
import com.example.KitchenIt.viewModel.EditUserActivity

import com.example.KitchenIt.viewModel.RecipeAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private val recipes = mutableListOf<Recipe>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra("title", recipe.title)
                putExtra("content", recipe.content)
                putExtra("imageUrl", recipe.imageUrl)
                putExtra("products", recipe.products)
                putExtra("userEmail", recipe.userEmail)
                putExtra("latitude", recipe.latitude)
                putExtra("longitude", recipe.longitude)
            }
            startActivity(intent)
        }

        recyclerView.adapter = recipeAdapter

        fetchRecipes()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    recipes.clear()
                    fetchRecipes()
                    true
                }
                R.id.action_my_recipes -> {
                    if (getSharedPreferences("UserSession", MODE_PRIVATE).getString("name", null) != null) {
                        recipes.clear()
                        fetchMyRecipes()
                        true
                    } else {
                        Toast.makeText(this, "No User Found! Please login first.", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.action_our_chef -> {
                    startActivity(Intent(this, RecipeListNewActivity::class.java))
                    true
                }
                R.id.action_map -> {
                    startActivity(Intent(this, RecipeMapActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRecipes() // Refresh recipes when returning to MainActivity
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        updateMenuVisibility(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        return when (item.itemId) {
            R.id.action_login_register -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.action_logout -> {
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                auth.signOut()
                updateMenuVisibility()
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.action_add_recipe -> {
                startActivity(Intent(this, AddRecipeActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, EditUserActivity::class.java))
                true
            }
            R.id.action_map -> {
                startActivity(Intent(this, RecipeMapActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMenuVisibility(menu: Menu? = null) {
        val currentUser = getSharedPreferences("UserSession", MODE_PRIVATE).getString("name", null)
        val loginRegisterItem = menu?.findItem(R.id.action_login_register)
        val logoutItem = menu?.findItem(R.id.action_logout)
        val addRecipeItem = menu?.findItem(R.id.action_add_recipe)
        val profileItem = menu?.findItem(R.id.action_profile)
        val mapItem = menu?.findItem(R.id.action_map)

        if (currentUser == null) {
            loginRegisterItem?.isVisible = true
            addRecipeItem?.isVisible = false
            profileItem?.isVisible = false
            logoutItem?.isVisible = false
            mapItem?.isVisible = false
        } else {
            loginRegisterItem?.isVisible = false
            addRecipeItem?.isVisible = true
            profileItem?.isVisible = true
            logoutItem?.isVisible = true
            mapItem?.isVisible = false
        }
    }

    private fun fetchRecipes() {
        db.collection("recipes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                recipes.clear()
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val products = document.getString("products") ?: ""
                    val userEmail = document.getString("userEmail") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0

                    val recipe = Recipe(title, content, imageUrl, products, userEmail, System.currentTimeMillis(), latitude, longitude)
                    recipes.add(recipe)
                }
                recipeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching recipes", exception)
            }
    }

    private fun fetchMyRecipes() {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("email", null)

        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User email not found in SharedPreferences", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "User email not found in SharedPreferences")
            return
        }
        db.collection("recipes")
            .whereEqualTo("userEmail", userEmail)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                recipes.clear()
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val products = document.getString("products") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0

                    val recipe = Recipe(title, content, imageUrl, products, userEmail, System.currentTimeMillis(), latitude, longitude)
                    recipes.add(recipe)
                }
                recipeAdapter.notifyDataSetChanged()
                if (recipes.size == 0) {
                    Toast.makeText(this, "No Such Recipes Yet!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching recipes: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching recipes", exception)
            }
    }
}
