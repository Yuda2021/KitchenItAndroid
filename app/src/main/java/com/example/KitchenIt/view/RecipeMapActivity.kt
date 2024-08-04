package com.example.KitchenIt.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.KitchenIt.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        loadRecipesOnMap()
    }

    private fun loadRecipesOnMap() {
        db.collection("recipes").get().addOnSuccessListener { result ->
            for (document in result) {
                val title = document.getString("title") ?: ""
                val latitude = document.getDouble("latitude") ?: 31.5
                val longitude = document.getDouble("longitude") ?: 34.75
                val location = LatLng(latitude, longitude)

                val marker = googleMap.addMarker(MarkerOptions().position(location).title(title))
                marker?.tag = document.id

                googleMap.setOnMarkerClickListener { marker ->
                    val recipeId = marker?.tag as? String
                    if (recipeId != null) {
                        val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                            putExtra("title", marker.title)
                            putExtra("latitude", location.latitude)
                            putExtra("longitude", location.longitude)
                        }
                        startActivity(intent)
                    }
                    true
                }
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(31.5, 34.75), 7f)) // Default to Israel
        }
    }
}
