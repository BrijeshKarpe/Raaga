package com.example.raaga.prahars

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raaga.data.Samay
import com.example.raaga.data.SamayList
import com.example.raaga.getAssetJsonData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*


class PraharsFragmentViewModel(application: Application) : AndroidViewModel(application){

    private var samayList = MutableLiveData<List<Samay>>()
    val _samayList : LiveData<List<Samay>>
    get() = samayList
    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main+viewModelJob)
    private var applicationContext: Context
    init {
        initializeSamay()
        applicationContext = application.applicationContext
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
    private fun initializeSamay(){

        uiScope.launch {

            samayList.value = getSamayFromJSON()
        }
    }
    private suspend fun getSamayFromJSON(): List<Samay>? {

        return withContext(Dispatchers.IO){
            val gson = Gson()
            val data: String? = getAssetJsonData(applicationContext)
            val type = object : TypeToken<SamayList>() {}.type

            val properties: SamayList = gson.fromJson(data, type)
            properties.samayRagaList
        }
    }
    private var _navigateToRagas = MutableLiveData<String>()
    val navigateToRagas : LiveData<String>
        get() = _navigateToRagas
    fun onSamayClicked(id:String){

        _navigateToRagas.value = id
    }
    fun onNavigateToRagas(){

        _navigateToRagas.value = null
    }
}