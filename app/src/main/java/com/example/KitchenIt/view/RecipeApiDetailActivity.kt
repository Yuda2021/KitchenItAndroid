// RecipeApiDetailActivity.kt
package com.example.KitchenIt.view

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.model.RecipeApiModel
import com.example.KitchenIt.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeApiDetailActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var nutritionTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var cookingTimeTextView: TextView
    private lateinit var servingsTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var dishTypesTextView: TextView
    private lateinit var dietsTextView: TextView
    private lateinit var cuisinesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_api_detail)

        titleTextView = findViewById(R.id.detailTitle)
        summaryTextView = findViewById(R.id.detailSummary)
        ingredientsTextView = findViewById(R.id.detailIngredients)
        nutritionTextView = findViewById(R.id.detailNutrition)
        imageView = findViewById(R.id.detailImage)
        cookingTimeTextView = findViewById(R.id.detailCookingTime)
        servingsTextView = findViewById(R.id.detailServings)
        instructionsTextView = findViewById(R.id.detailInstructions)
        dishTypesTextView = findViewById(R.id.detailDishTypes)
        dietsTextView = findViewById(R.id.detailDiets)
        cuisinesTextView = findViewById(R.id.detailCuisines)

        val recipeId = intent.getIntExtra("recipeId", -1)

        if (recipeId != -1) {
            fetchRecipeDetails(recipeId)
        } else {
            Toast.makeText(this, "Invalid recipe ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchRecipeDetails(recipeId: Int) {
        RetrofitClient.instance.getRecipe(recipeId, "731232ba92804023a163fafbbfacd26c").enqueue(object : Callback<RecipeApiModel> {
            override fun onResponse(
                call: Call<RecipeApiModel>,
                response: Response<RecipeApiModel>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipe ->
                        titleTextView.text = recipe.title
                        summaryTextView.text = HtmlCompat.fromHtml(recipe.summary, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        ingredientsTextView.text = HtmlCompat.fromHtml(
                            "<b>Ingredients:</b><br><ul>" + (recipe.extendedIngredients?.joinToString("") { "<li>${it.original}</li>" } ?: "No ingredients available") + "</ul>",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        nutritionTextView.text = HtmlCompat.fromHtml(
                            "<b>Nutrition:</b><br><ul>" + (recipe.nutrition?.nutrients?.joinToString("") { "<li>${it.name}: ${it.amount} ${it.unit}</li>" } ?: "No nutrition information available") + "</ul>",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        cookingTimeTextView.text = getString(R.string.cooking_time) + (recipe.cookingMinutes?.toString() ?: "Unknown")
                        servingsTextView.text = getString(R.string.servings) + (recipe.servings?.toString() ?: "Unknown")
                        instructionsTextView.text = HtmlCompat.fromHtml("<b>Instructions:</b><br>" + (recipe.instructions ?: "No instructions available"), HtmlCompat.FROM_HTML_MODE_LEGACY)
                        dishTypesTextView.text = getString(R.string.dish_types) + (recipe.dishTypes?.joinToString(", ") ?: "No dish types available")
                        dietsTextView.text = getString(R.string.diets) + (recipe.diets?.joinToString(", ") ?: "No diets available")
                        cuisinesTextView.text = getString(R.string.cuisines) + (recipe.cuisines?.joinToString(", ") ?: "No cuisines available")
                        Glide.with(this@RecipeApiDetailActivity).load(recipe.image).into(imageView)
                    }
                } else {
                    Toast.makeText(this@RecipeApiDetailActivity, "Failed to fetch recipe details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeApiModel>, t: Throwable) {
                Toast.makeText(this@RecipeApiDetailActivity, "Error fetching recipe details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
