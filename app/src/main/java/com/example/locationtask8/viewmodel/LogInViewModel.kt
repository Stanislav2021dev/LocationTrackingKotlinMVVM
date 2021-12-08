package com.example.locationtask8.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.locationtask8.model.LogInModel
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception

class LogInViewModel(application: Application) : AndroidViewModel(application) {
    private val logInModel:LogInModel
    private lateinit var userLogInLiveData:MutableLiveData<FirebaseUser?>
    private lateinit var userSignUpLiveData:MutableLiveData<FirebaseUser?>
    private lateinit var exceptionLiveData:MutableLiveData<Exception?>


    init {
        logInModel= LogInModel(application)
        getLiveData()
    }

    fun logIn(email:String,password:String) {
        logInModel.logInUser(email, password)
        getLiveData()
    }

    fun signUp(email: String,password: String) {
        logInModel.signUpUser(email, password)
        getLiveData()
    }

    fun getLiveData(){
        userLogInLiveData=logInModel.getUserLogInLiveData()
        userSignUpLiveData=logInModel.getUserSignUpLiveData()
        exceptionLiveData=logInModel.getExceptionLiveData()
    }

    fun getUserLogInLiveData()=userLogInLiveData
    fun getUserSignUpLiveData() = userSignUpLiveData
    fun getExceptionLiveData()=exceptionLiveData


}