package com.example.locationtask8.view

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.locationtask8.R
import com.example.locationtask8.databinding.FragmentLoginBinding
import com.example.locationtask8.di.AppComponent
import com.example.locationtask8.di.DaggerAppComponent
import com.example.locationtask8.model.LogInModel
import com.example.locationtask8.viewmodel.LogInViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class LogInFragment: Fragment() {


    @Inject lateinit var auth: FirebaseAuth
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var logInViewModel : LogInViewModel=LogInViewModel(application = Application())
    private lateinit var nav:NavController

    init {
        val appComponent: AppComponent = DaggerAppComponent.create()
        appComponent.inject(this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logInViewModel =
            ViewModelProvider(this).get(LogInViewModel::class.java)

        logInViewModel.getUserLogInLiveData().observe(this, Observer {
            Log.v("User", "User "+ it)
            authSuccess( getString(R.string.login_success),it)
        })

        logInViewModel.getUserSignUpLiveData().observe(this,{
               authSuccess(getString(R.string.account_created),it)
        })
        logInViewModel.getExceptionLiveData().observe(this, Observer {
            if (it != null) {
                Toast.makeText(context,getString(R.string.auth_fail)+it.message,Toast.LENGTH_LONG).show()
            }
        })
    }


    fun getEmail() :String = binding.textInputEmail.text.toString()
    fun getPassword():String = binding.textInputPassword.text.toString()

    fun validateEmail():Boolean {
        if (getEmail().isEmpty()){
            binding.textInputEmail.setError(getString(R.string.empty_email))
            return false
        }
        else{
            binding.textInputEmail.setError(null,null)
            return true
        }
    }

    fun validatePassword():Boolean{
        if (getPassword().isEmpty()){
            binding.textInputPassword.setError(getString(R.string.empty_password))
            return false
        }
        else{
            binding.textInputPassword.setError(null,null)
            return true
        }
    }


    fun authSuccess(msg:String, user:FirebaseUser?){
        if (user!=null){
            Toast.makeText(context,msg ,Toast.LENGTH_LONG).show()
          //  nav  = Navigation.findNavController(requireView())

           // nav.navigate(LogInFragmentDirections.actionLogInFragmentToMapsFragment())
           // nav.popBackStack(R.id.logInFragment,true)

            val manager: FragmentManager = requireActivity().supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.addToBackStack("ll")
            transaction.replace(R.id.activity_main_navHostFragment, MapsFragment())
            transaction.commit()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        inflater.inflate(R.layout.fragment_login,container,false)
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth.currentUser!=null){
            Navigation.findNavController(requireView()).navigate(R.id.action_logInFragment_to_mapsFragment)
        }
        binding.loginButton.setOnClickListener {
            if (validateEmail() and  validatePassword()) {
                logInViewModel.logIn(getEmail(),getPassword())
            }

        }
        binding.singUpButton.setOnClickListener{
            if (validateEmail() and validatePassword()){
                logInViewModel.signUp(getEmail(),getPassword())
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null

    }
}