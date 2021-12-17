package com.example.locationtask8.view

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.google.android.material.snackbar.Snackbar

import javax.inject.Inject

class SnackBarView @Inject constructor() : Activity() {

    fun createSnackBar(context: Context, maitText:String, action:String, intent: Intent) :Snackbar{
        val snackbar=Snackbar.make(context, (context as Activity).findViewById(R.id.content),
                        maitText, Snackbar.LENGTH_INDEFINITE).setAction(action, {
            finish()
            context.startActivity(intent)
        })
        snackbar.show()
        return snackbar
    }
}