package com.example.KitchenIt.view

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.api.RecipeDetailResponse
import com.example.KitchenIt.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeApiDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_api_detail)

        titleTextView = findViewById(R.id.detailTitle)
        contentTextView = findViewById(R.id.detailContent)
        imageView = findViewById(R.id.detailImage)

        val recipeId = intent.getStringExtra("recipeId") ?: return

        fetchRecipeDetails(recipeId)
    }

    private fun fetchRecipeDetails(recipeId: String) {
        RetrofitClient.instance.getRecipe(recipeId, "731232ba92804023a163fafbbfacd26c").enqueue(object : Callback<RecipeDetailResponse> {
            override fun onResponse(
                call: Call<RecipeDetailResponse>,
                response: Response<RecipeDetailResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipe ->
                        titleTextView.text = recipe.title
                        contentTextView.text = HtmlCompat.fromHtml(recipe.instructions, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        contentTextView.movementMethod = LinkMovementMethod.getInstance()
                        Glide.with(this@RecipeApiDetailActivity).load(recipe.image).into(imageView)
                    }
                } else {
                    Toast.makeText(this@RecipeApiDetailActivity, "Failed to fetch recipe details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                Toast.makeText(this@RecipeApiDetailActivity, "Error fetching recipe details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
