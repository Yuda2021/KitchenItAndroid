package com.example.KitchenIt.viewModel

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.Recipe
import com.example.KitchenIt.view.RecipeDetailActivity

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitle)
        private val imageView: ImageView = itemView.findViewById(R.id.recipeImage)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title
            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .into(imageView)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("title", recipe.title)
                    putExtra("content", recipe.content)
                    putExtra("imageUrl", recipe.imageUrl)
                    putExtra("products", recipe.products)
                    putExtra("userEmail", recipe.userEmail)
                    putExtra("latitude", recipe.latitude)
                    putExtra("longitude", recipe.longitude)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
