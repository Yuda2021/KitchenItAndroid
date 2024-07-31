package com.example.KitchenIt.api

data class RecipeDetailResponse(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String,
    val instructions: String
)
