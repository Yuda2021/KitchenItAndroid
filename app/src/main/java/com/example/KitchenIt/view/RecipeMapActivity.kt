package com.example.KitchenIt.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.example.KitchenIt.Recipe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class RecipeMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private lateinit var buttonZoomIn: Button
    private lateinit var buttonZoomOut: Button
    private lateinit var buttonShowRecipes: Button
    private lateinit var recipes: List<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonZoomIn = findViewById(R.id.buttonZoomIn)
        buttonZoomOut = findViewById(R.id.buttonZoomOut)
        buttonShowRecipes = findViewById(R.id.buttonShowRecipes)

        buttonZoomIn.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        buttonZoomOut.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut())
        }

        buttonShowRecipes.setOnClickListener {
            showRecipesModal()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.isZoomControlsEnabled = true
        loadRecipesOnMap()
    }

    private fun loadRecipesOnMap() {
        db.collection("recipes").get().addOnSuccessListener { result ->
            recipes = result.mapNotNull { document ->
                val title = document.getString("title") ?: ""
                val content = document.getString("content") ?: ""
                val imageUrl = document.getString("imageUrl") ?: ""
                val products = document.getString("products") ?: ""
                val userEmail = document.getString("userEmail") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                if (latitude != 0.0 && longitude != 0.0) {
                    Recipe(title, content, imageUrl, products, userEmail, 0L, latitude, longitude)
                } else {
                    null
                }
            }

            recipes.forEach { recipe ->
                val location = LatLng(recipe.latitude, recipe.longitude)
                val marker = googleMap.addMarker(MarkerOptions().position(location).title(recipe.title))
                marker?.tag = recipe
            }

            googleMap.setOnMarkerClickListener { marker ->
                val recipe = marker.tag as? Recipe
                if (recipe != null) {
                    val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                        putExtra("title", recipe.title)
                        putExtra("content", recipe.content)
                        putExtra("imageUrl", recipe.imageUrl)
                        putExtra("products", recipe.products)
                        putExtra("userEmail", recipe.userEmail)
                    }
                    startActivity(intent)
                }
                true
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(31.5, 34.75), 7f)) // Default to Israel
        }
    }

    private fun showRecipesModal() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_recipe_list, null)
        val recyclerView = dialogLayout.findViewById<RecyclerView>(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dialog = builder.setView(dialogLayout)
            .setTitle("Select a Recipe")
            .setNegativeButton("Close", null)
            .create()

        val adapter = RecipeListAdapter(recipes) { recipe ->
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(recipe.latitude, recipe.longitude), 15f))
            dialog.dismiss()
        }
        recyclerView.adapter = adapter

        dialog.show()
    }

    class RecipeListAdapter(private val recipes: List<Recipe>, private val onItemClick: (Recipe) -> Unit) :
        RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

        inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitle)
            private val imageView: ImageView = itemView.findViewById(R.id.recipeImage)

            fun bind(recipe: Recipe) {
                titleTextView.text = recipe.title
                Glide.with(itemView.context).load(recipe.imageUrl).into(imageView)

                itemView.setOnClickListener {
                    onItemClick(recipe)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recipe_modal, parent, false)
            return RecipeViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            holder.bind(recipes[position])
        }

        override fun getItemCount() = recipes.size
    }
}
