package com.example.KitchenIt.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.KitchenIt.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class RecipeMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private lateinit var buttonZoomIn: Button
    private lateinit var buttonZoomOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonZoomIn = findViewById(R.id.buttonZoomIn)
        buttonZoomOut = findViewById(R.id.buttonZoomOut)

        buttonZoomIn.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        buttonZoomOut.setOnClickListener {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.isZoomControlsEnabled = true
        loadRecipesOnMap()
    }

    private fun loadRecipesOnMap() {
        db.collection("recipes").get().addOnSuccessListener { result ->
            for (document in result) {
                val title = document.getString("title") ?: ""
                val country = document.getString("country") ?: ""
                val city = document.getString("city") ?: ""
                val location = getRandomLocationInCity(city, country)

                location?.let {
                    val marker = googleMap.addMarker(MarkerOptions().position(it).title(title))
                    marker?.tag = document.id

                    googleMap.setOnMarkerClickListener { marker ->
                        val recipeId = marker?.tag as? String
                        if (recipeId != null) {
                            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                                putExtra("title", marker.title)
                                putExtra("latitude", it.latitude)
                                putExtra("longitude", it.longitude)
                            }
                            startActivity(intent)
                        }
                        true
                    }
                }
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(31.5, 34.75), 7f)) // Default to Israel
        }
    }

    private fun getRandomLocationInCity(city: String, country: String): LatLng? {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocationName("$city, $country", 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val lat = address.latitude
            val lng = address.longitude

            // Generate a random position within a 0.01 degree (~1 km) radius
            val randomLat = lat + (Random.nextDouble() - 0.5) * 0.02
            val randomLng = lng + (Random.nextDouble() - 0.5) * 0.02

            return LatLng(randomLat, randomLng)
        }
        return null
    }
}
