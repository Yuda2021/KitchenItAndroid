// RecipeApiService.kt
package com.example.KitchenIt.api

import com.example.KitchenIt.model.RecipeApiModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApiService {
    @GET("recipes/random")
    fun getRandomRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int
    ): Call<RecipeResponse>

    @GET("recipes/{id}/information")
    fun getRecipe(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): Call<RecipeApiModel>
}
