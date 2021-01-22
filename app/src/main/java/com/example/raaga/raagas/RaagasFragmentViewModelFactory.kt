package com.example.raaga.raagas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RaagasFragmentViewModelFactory (
    private val samay_key: String,
    private val application: Application
):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RaagasFragmentViewModel::class.java)){

            return RaagasFragmentViewModel(samay_key,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}