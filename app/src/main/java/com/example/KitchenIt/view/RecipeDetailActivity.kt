package com.example.KitchenIt.view

import android.os.Bundle
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

class RecipeDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var productsTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var userEmailTextView: TextView
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
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val products = intent.getStringExtra("products") ?: ""
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 31.5)
        val longitude = intent.getDoubleExtra("longitude", 34.75)

        recipeLocation = LatLng(latitude, longitude)

        titleTextView.text = title
        contentTextView.text = content
        productsTextView.text = products
        userEmailTextView.text = "Posted by: $userEmail"
        Glide.with(this).load(imageUrl).into(imageView)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        recipeLocation?.let {
            googleMap.addMarker(MarkerOptions().position(it).title("Recipe Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
        }
    }
}
