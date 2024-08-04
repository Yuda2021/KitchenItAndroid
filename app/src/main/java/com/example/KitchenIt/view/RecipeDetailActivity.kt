// RecipeDetailActivity.kt
package com.example.KitchenIt.view

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.KitchenIt.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class RecipeDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var productsTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var userEmailTextView: TextView
    private lateinit var locationMessageTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var cityTextView: TextView
    private lateinit var mapFragment: SupportMapFragment
    private var recipeLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        titleTextView = findViewById(R.id.detailTitle)
        contentTextView = findViewById(R.id.detailContent)
        productsTextView = findViewById(R.id.detailProducts)
        imageView = findViewById(R.id.detailImage)
        userEmailTextView = findViewById(R.id.detailUserEmail)
        locationMessageTextView = findViewById(R.id.locationMessage)
        countryTextView = findViewById(R.id.detailCountry)
        cityTextView = findViewById(R.id.detailCity)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val products = intent.getStringExtra("products") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        Log.d("RecipeDetailActivity", "Recipe data - title: $title, content: $content, imageUrl: $imageUrl, products: $products, userEmail: $userEmail, latitude: $latitude, longitude: $longitude")

        if (latitude != 0.0 && longitude != 0.0) {
            recipeLocation = LatLng(latitude, longitude)
            reverseGeocodeLocation(recipeLocation!!)
        } else {
            Log.d("RecipeDetailActivity", "Latitude and/or longitude are zero, setting recipeLocation to null.")
        }

        titleTextView.text = title
        contentTextView.text = content
        productsTextView.text = products
        userEmailTextView.text = "Posted by: $userEmail"
        Glide.with(this).load(imageUrl).into(imageView)

        if (recipeLocation == null) {
            locationMessageTextView.text = "Location not available for this recipe. Edit the Recipe to add location."
            locationMessageTextView.visibility = View.VISIBLE
        } else {
            locationMessageTextView.visibility = View.GONE
        }

        // Add the SupportMapFragment programmatically
        mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    private fun reverseGeocodeLocation(location: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addresses?.let {
            if (it.isNotEmpty()) {
                val address = it[0]
                countryTextView.text = "Country: ${address.countryName}"
                cityTextView.text = "City: ${address.locality ?: address.subAdminArea}"
                Log.d("RecipeDetailActivity", "Geocoded address - Country: ${address.countryName}, City: ${address.locality ?: address.subAdminArea}")
            } else {
                Log.d("RecipeDetailActivity", "Geocoder returned an empty list of addresses.")
                countryTextView.text = "Country: N/A"
                cityTextView.text = "City: N/A"
            }
        } ?: run {
            Log.d("RecipeDetailActivity", "Geocoder returned null.")
            countryTextView.text = "Country: N/A"
            cityTextView.text = "City: N/A"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        recipeLocation?.let {
            googleMap.addMarker(MarkerOptions().position(it).title("Recipe Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            Log.d("RecipeDetailActivity", "Marker added to map at location: $it")
        } ?: run {
            Log.d("RecipeDetailActivity", "Recipe location is null, no marker added.")
        }
    }
}
