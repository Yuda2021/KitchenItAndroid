package com.example.KitchenIt.api

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
        @Path("id") id: String,
        @Query("apiKey") apiKey: String
    ): Call<RecipeDetailResponse>
}
