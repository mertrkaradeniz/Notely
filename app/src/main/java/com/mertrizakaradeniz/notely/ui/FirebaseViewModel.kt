package com.mertrizakaradeniz.notely.ui

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.storage.UploadTask
import com.mertrizakaradeniz.notely.data.repository.FirebaseRepository
import com.mertrizakaradeniz.notely.util.Constant.USER_EMAIL
import com.mertrizakaradeniz.notely.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _signUpResult = MutableLiveData<Resource<Task<AuthResult>>>()
    val signUpResult: LiveData<Resource<Task<AuthResult>>> = _signUpResult

    private val _signInResult = MutableLiveData<Resource<Task<AuthResult>>>()
    val signInResult: LiveData<Resource<Task<AuthResult>>> = _signInResult

    private val _fileUploadResult = MutableLiveData<Resource<UploadTask.TaskSnapshot>>()
    val fileUploadResult: LiveData<Resource<UploadTask.TaskSnapshot>> = _fileUploadResult

    private val _filePath = MutableLiveData<String>()
    val filePath: LiveData<String> = _filePath

    fun signUp(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.signUp(email, password) { resource ->
                _signUpResult.postValue(resource)
            }
        }
    }

    fun signIn(email: String, password: String) {
        firebaseRepository.signIn(email, password) { resource ->
            _signInResult.postValue(resource)
            if (resource.data?.isSuccessful == true) {
                sharedPreferences.edit().putString(USER_EMAIL, email).apply()
            }
        }
    }

    fun upload(fileUri: Uri) {
        firebaseRepository.upload(fileUri) { resource ->
            getFilePath(fileUri)
            _fileUploadResult.postValue(resource)
        }
    }

    /*fun download(fileUri: Uri) {
        firebaseRepository.download(fileUri) { resource ->
            _filePath.postValue(resource.data)
        }
    }*/

    private fun getFilePath(fileUri: Uri) {
        firebaseRepository.getFirebaseStorage()
            .child(fileUri.lastPathSegment.toString()).downloadUrl.addOnSuccessListener { Uri ->
                _filePath.postValue(Uri.toString())
            }.addOnFailureListener { exception ->
                _filePath.postValue(exception.localizedMessage)
            }
    }
}

