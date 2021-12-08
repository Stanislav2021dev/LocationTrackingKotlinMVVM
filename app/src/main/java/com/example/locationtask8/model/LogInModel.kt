package com.example.locationtask8.model

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception

class LogInModel (_app:Application) {
    private val app:Application
    private var userLogInLiveData : MutableLiveData<FirebaseUser?>
    private var userSingUpLiveData: MutableLiveData<FirebaseUser?>
    private var exceptionLiveData :MutableLiveData<Exception?>
    private var firebaseAuth :FirebaseAuth

    init {
        app=_app
        firebaseAuth= FirebaseAuth.getInstance()
        userLogInLiveData = MutableLiveData()
        exceptionLiveData = MutableLiveData()
        userSingUpLiveData=MutableLiveData()

    }

    fun logInUser (email:String,password:String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(app.mainExecutor) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithEmail:success")
                    userLogInLiveData.postValue(firebaseAuth.currentUser)

                } else {
                    Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                    userLogInLiveData.postValue(null)
                    exceptionLiveData.postValue(task.exception)
                }
            }
        }

    fun signUpUser(email: String,password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(app.mainExecutor){task ->
                if (task.isSuccessful) {
                    userSingUpLiveData.postValue(firebaseAuth.currentUser)
                }
                else
                    userSingUpLiveData.postValue(null)
                    exceptionLiveData.postValue(task.exception)
            }

    }

    fun getUserLogInLiveData() = userLogInLiveData
    fun getUserSignUpLiveData() = userSingUpLiveData
    fun getExceptionLiveData() = exceptionLiveData
}