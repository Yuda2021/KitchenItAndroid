// RecipeListNewActivity.kt
package com.example.KitchenIt.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.KitchenIt.R
import com.example.KitchenIt.api.RecipeResponse
import com.example.KitchenIt.api.RetrofitClient
import com.example.KitchenIt.viewModel.RecipeListNewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeListNewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeListNewAdapter
    private val apiKey = "731232ba92804023a163fafbbfacd26c"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list_new)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RecipeListNewAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeApiDetailActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fetchRecipes()
    }

    private fun fetchRecipes() {
        RetrofitClient.instance.getRandomRecipes(apiKey, 10).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.recipes?.let { recipes ->
                        adapter.updateRecipes(recipes)
                    }
                } else {
                    Toast.makeText(this@RecipeListNewActivity, "Failed to fetch recipes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.e("RecipeListNewActivity", "Error fetching recipes", t)
                Toast.makeText(this@RecipeListNewActivity, "Error fetching recipes", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
