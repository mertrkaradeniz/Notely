package com.mertrizakaradeniz.notely.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mertrizakaradeniz.notely.util.Constant.UPLOADED_FILES
import com.mertrizakaradeniz.notely.util.Resource
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {
    fun getCurrentUser() = firebaseAuth.currentUser
    fun getFirebaseStorage() = firebaseStorage.getReference(UPLOADED_FILES)

    fun signUp(email: String, password: String, onResult: (Resource<Task<AuthResult>>) -> Unit) {
        onResult(Resource.Loading())
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(Resource.Success(task))
            } else {
                onResult(Resource.Error("Authentication failed."))
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Resource<Task<AuthResult>>) -> Unit) {
        onResult(Resource.Loading())
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(Resource.Success(task))
            } else {
                onResult(Resource.Error(task.exception?.localizedMessage ?: ""))
            }
        }
    }

    fun upload(fileUri: Uri, onResult: (Resource<UploadTask.TaskSnapshot>) -> Unit) {
        onResult(Resource.Loading())
        getFirebaseStorage().child(fileUri.lastPathSegment!!)
            .putFile(fileUri).addOnSuccessListener { task ->
                onResult(Resource.Success(task))
            }.addOnFailureListener { exception ->
                onResult(Resource.Error(exception.localizedMessage ?: ""))
            }
    }

    /*fun download(fileUri: Uri, onResult: (Resource<Uri>) -> Unit) {
        onResult(Resource.Loading())
        getFirebaseStorage().child(fileUri.lastPathSegment.toString()).downloadUrl.addOnSuccessListener { Uri ->
            onResult(Resource.Success(Uri))
        }.addOnFailureListener { exception ->
            onResult(Resource.Error(exception.localizedMessage ?: ""))
        }
    }*/
}