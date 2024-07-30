package com.example.KitchenIt.viewModel

import com.example.KitchenIt.Recipe

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot



class DBHelper {

    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    // Get all recipes
    fun getAllRecipes(onSuccess: (List<Recipe>) -> Unit, onFailure: (Exception) -> Unit) {
        recipesCollection.get()
            .addOnSuccessListener { result ->
                val recipes = result.mapNotNull { it.toRecipe() }
                onSuccess(recipes)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Get recipe by title and user email
    fun getRecipeByTitleAndUserEmail(title: String, userEmail: String, onSuccess: (Recipe?) -> Unit, onFailure: (Exception) -> Unit) {
        recipesCollection.whereEqualTo("title", title)
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { result ->
                val recipe = result.documents.firstOrNull()?.toRecipe()
                onSuccess(recipe)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Get recipes by user email
    fun getRecipesByUserEmail(userEmail: String, onSuccess: (List<Recipe>) -> Unit, onFailure: (Exception) -> Unit) {
        recipesCollection.whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.mapNotNull { it.toRecipe() }
                onSuccess(recipes)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Add a new recipe
    fun addRecipe(recipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        recipesCollection.add(recipe)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Update recipe by title and user email
    fun updateRecipeByTitleAndUserEmail(title: String, userEmail: String, newRecipe: Recipe, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        getRecipeByTitleAndUserEmail(title, userEmail, { recipe ->
            recipe?.let {
                recipesCollection.whereEqualTo("title", title)
                    .whereEqualTo("userEmail", userEmail)
                    .get()
                    .addOnSuccessListener { result ->
                        val documentId = result.documents.firstOrNull()?.id
                        documentId?.let {
                            recipesCollection.document(it)
                                .set(newRecipe)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } ?: onFailure(Exception("Recipe not found"))
        }, onFailure)
    }

    // Delete recipe by title and user email
    fun deleteRecipeByTitleAndUserEmail(title: String, userEmail: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        getRecipeByTitleAndUserEmail(title, userEmail, { recipe ->
            recipe?.let {
                recipesCollection.whereEqualTo("title", title)
                    .whereEqualTo("userEmail", userEmail)
                    .get()
                    .addOnSuccessListener { result ->
                        val documentId = result.documents.firstOrNull()?.id
                        documentId?.let {
                            recipesCollection.document(it)
                                .delete()
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } ?: onFailure(Exception("Recipe not found"))
        }, onFailure)
    }

    // Extension function to convert DocumentSnapshot to Recipe
    private fun DocumentSnapshot.toRecipe(): Recipe? {
        return try {
            this.toObject(Recipe::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
