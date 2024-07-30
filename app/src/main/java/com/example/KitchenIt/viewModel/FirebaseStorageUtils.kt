package com.example.KitchenIt.viewModel

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

object FirebaseStorageUtils {

    // Function to upload a file to Firebase Storage
    fun uploadFile(fileUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val fileName = UUID.randomUUID().toString() // Create a unique file name
        val fileRef = storageRef.child("images/$fileName") // Path to store file

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    // File URL is returned
                    onSuccess(uri.toString())
                }.addOnFailureListener {
                    onFailure(it)
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
