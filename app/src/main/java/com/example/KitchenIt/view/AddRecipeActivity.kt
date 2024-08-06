// AddRecipeActivity.kt
package com.example.KitchenIt.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.KitchenIt.R
import com.example.KitchenIt.viewModel.FirebaseStorageUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var editTextProducts: EditText
    private lateinit var editTextCountry: EditText
    private lateinit var editTextCity: EditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonUseCurrentLocation: Button
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        editTextProducts = findViewById(R.id.editTextProducts)
        editTextCountry = findViewById(R.id.editTextCountry)
        editTextCity = findViewById(R.id.editTextCity)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonUseCurrentLocation = findViewById(R.id.buttonUseCurrentLocation)
        progressBar = findViewById(R.id.progressBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString()
            val content = editTextContent.text.toString()
            val products = editTextProducts.text.toString()
            val country = editTextCountry.text.toString()
            val city = editTextCity.text.toString()
            val userEmail = getUserEmail(this)

            if (title.isNotEmpty() && content.isNotEmpty() && products.isNotEmpty() && imageUri != null) {
                progressBar.visibility = View.VISIBLE
                buttonSave.isEnabled = false
                buttonCancel.isEnabled = false
                FirebaseStorageUtils.uploadFile(imageUri!!, { imageUrl ->
                    if (country.isNotEmpty() && city.isNotEmpty()) {
                        val location = getLocationFromAddress(country, city)
                        if (location != null) {
                            saveRecipeToFirestore(title, content, imageUrl, products, userEmail, location)
                        } else {
                            progressBar.visibility = View.GONE
                            buttonSave.isEnabled = true
                            buttonCancel.isEnabled = true
                            Toast.makeText(this, "Invalid country or city", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        getLocation { location ->
                            val latLng = location ?: LatLng(31.5, 34.75) // Default to Israel if location not available
                            saveRecipeToFirestore(title, content, imageUrl, products, userEmail, latLng)
                        }
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    buttonSave.isEnabled = true
                    buttonCancel.isEnabled = true
                    Toast.makeText(
                        this,
                        "Error uploading file: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            } else {
                Toast.makeText(
                    this,
                    "Please fill out all fields and select an image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        buttonCancel.setOnClickListener {
            finish()
        }

        buttonUseCurrentLocation.setOnClickListener {
            getLocation { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        editTextCountry.setText(address.countryName)
                        editTextCity.setText(address.locality)
                    }
                }
            }
        }

        checkLocationPermission()

        // Set default image if none is selected
        imageView.setImageResource(R.drawable.ic_placeholder)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
            imageView.visibility = View.VISIBLE
        }
    }

    private fun saveRecipeToFirestore(title: String, content: String, imageUrl: String, products: String, userEmail: String?, location: LatLng) {
        val db = FirebaseFirestore.getInstance()
        val recipe = hashMapOf(
            "title" to title,
            "content" to content,
            "imageUrl" to imageUrl,
            "products" to products,
            "userEmail" to userEmail,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()  // Add timestamp
        )

        db.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                buttonSave.isEnabled = true
                buttonCancel.isEnabled = true
                Toast.makeText(this, "Recipe saved", Toast.LENGTH_SHORT).show()

                // Restart the main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                buttonSave.isEnabled = true
                buttonCancel.isEnabled = true
                Toast.makeText(this, "Error saving recipe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserEmail(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email", null)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLocation {
                it?.let {
                    currentLocation = it
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        editTextCountry.setText(address.countryName)
                        editTextCity.setText(address.locality)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(onComplete: ((LatLng?) -> Unit)?) {
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val location: Location? = task.result
                currentLocation = LatLng(location!!.latitude, location.longitude)
                onComplete?.invoke(currentLocation)
            } else {
                onComplete?.invoke(null)
            }
        }
    }

    private fun getLocationFromAddress(country: String, city: String): LatLng? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocationName("$city, $country", 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            val location = addresses[0]
            LatLng(location.latitude, location.longitude)
        } else {
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLocation {
                        it?.let {
                            currentLocation = it
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val address = addresses[0]
                                editTextCountry.setText(address.countryName)
                                editTextCity.setText(address.locality)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
