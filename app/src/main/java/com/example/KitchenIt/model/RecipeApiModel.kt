// RecipeApiModel.kt
package com.example.KitchenIt.model

data class RecipeApiModel(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String,
    val extendedIngredients: List<ExtendedIngredient>?,
    val nutrition: Nutrition?,
    val cookingMinutes: Int?,
    val servings: Int?,
    val instructions: String?,
    val dishTypes: List<String>?,
    val diets: List<String>?,
    val cuisines: List<String>?
)

data class ExtendedIngredient(
    val original: String
)

data class Nutrition(
    val nutrients: List<Nutrient>?
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)
