package com.example.locationtask8.model

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.provider.SyncStateContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import javax.inject.Inject

class LogInModel () {
    @Inject
    lateinit var context: Context
    private var userLogInLiveData : MutableLiveData<FirebaseUser?>
    private var userSingUpLiveData: MutableLiveData<FirebaseUser?>
    private var exceptionLiveData :MutableLiveData<Exception?>
    private var firebaseAuth :FirebaseAuth

    private var task: Task<AuthResult>?=null

    private val ref:DatabaseReference?=null
    private lateinit var listener :ValueEventListener

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)
        firebaseAuth= FirebaseAuth.getInstance()
        userLogInLiveData = MutableLiveData()
        exceptionLiveData = MutableLiveData()
        userSingUpLiveData=MutableLiveData()

    }

    fun logInUser (email:String,password:String) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(context.mainExecutor) { task ->
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
            .addOnCompleteListener(context.mainExecutor){task ->
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


