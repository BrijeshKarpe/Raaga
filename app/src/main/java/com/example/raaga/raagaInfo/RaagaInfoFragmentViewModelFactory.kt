package com.example.raaga.raagaInfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RaagaInfoFragmentViewModelFactory (

    private val raga_key:String,
    private val samay_key:String,
    private val application: Application
):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(RaagaInfoFragmentViewModel::class.java)){

            return RaagaInfoFragmentViewModel(raga_key,samay_key,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}