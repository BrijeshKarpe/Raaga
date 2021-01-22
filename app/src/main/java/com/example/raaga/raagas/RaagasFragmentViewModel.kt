package com.example.raaga.raagas

import android.app.Application
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raaga.IMainActivity
import com.example.raaga.data.Raga
import com.example.raaga.data.Samay
import com.example.raaga.data.SamayList
import com.example.raaga.getAssetJsonData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class RaagasFragmentViewModel(
    val samay_key : String,
    application: Application
) : AndroidViewModel(application){

    lateinit var mSelectedMedia:MediaMetadataCompat
    lateinit var mIMainActivity:IMainActivity
    var mCurrentPlaylist:ArrayList<MediaMetadataCompat> = ArrayList<MediaMetadataCompat>()
    private var samay = MutableLiveData<Samay>()
    var currentRaga = MutableLiveData<String>()
    var timeFrom = MutableLiveData<String>()
    var timeTo = MutableLiveData<String>()
    private var _ragaList = MutableLiveData<List<Raga>>()
    val ragaList : LiveData<List<Raga>>
        get() = _ragaList
    val _samay : LiveData<Samay>
        get() = samay
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

            samay.value = getSamayFromJSON(samay_key)

            Log.i("RaagasFragmentViewModel", mCurrentPlaylist.size.toString())
        }
    }

    private fun createPlaylist(samay:Samay):ArrayList<MediaMetadataCompat>{

        Log.i("RaagasFragment","createPlaylist Called")
        val playlist:ArrayList<MediaMetadataCompat> = ArrayList<MediaMetadataCompat>()
        for(raga:Raga in samay.raga){

            playlist.add(MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,(""+raga.id+""+raga.sid+""))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,raga.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,raga.thaat)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, Uri.parse(raga.audio).toString())
                .build())
        }
        return playlist
    }

    private suspend fun getSamayFromJSON(samay_key: String): Samay? {

        return withContext(Dispatchers.IO){
            val gson = Gson()
            val data: String? = getAssetJsonData(applicationContext)
            val type = object : TypeToken<SamayList>() {}.type

            val properties: SamayList = gson.fromJson(data, type)
            properties.samayRagaList.get(Integer.parseInt(samay_key)-1)
        }
    }
    fun updateUI(){

        currentRaga.value = (samay.value?.name)?.capitalize()
        timeFrom.value = samay.value?.time_from
        timeTo.value = samay.value?.time_to
        _ragaList.value = samay.value?.raga
        val last:MediaMetadataCompat? = mIMainActivity.getMyApplication().getMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia()!!)
        if(last != null){setPlayButton(last)}
    }
    private val _navigateToRaagaInfo = MutableLiveData<String>()
    val navigateToRaagaInfo : LiveData<String>
        get() = _navigateToRaagaInfo
    fun onRagaClicked(id : String){


        _navigateToRaagaInfo.value = id

            //Log.i("Playlist",id)
            mCurrentPlaylist = createPlaylist(samay.value!!)
            mIMainActivity.getMyApplication().setMediaItems(mCurrentPlaylist)
            mSelectedMedia = mCurrentPlaylist[Integer.parseInt(id) - 1]
            //if(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia() != (id+samay_key)) {

            mIMainActivity.onMediaSelected(samay_key, mSelectedMedia, Integer.parseInt(id) - 1)

            //}
            saveLastPlayedSongProperties()
    }
    fun onNavigateToRaagaInfo(){

        _navigateToRaagaInfo.value = null
    }
    fun updateMainActivity(activity: IMainActivity){

        mIMainActivity = activity

    }
    fun saveLastPlayedSongProperties(){

        mIMainActivity.getMyPreferenceManager().setPlaylistId(samay.value!!.id)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedArtist(samay_key)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedCategory(currentRaga.value)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.description.mediaId)
    }
    fun setPlayButton(mediaItem: MediaMetadataCompat) {


        val id = mediaItem.description.mediaId
        val samayId = _samay.value!!.id
        val ragaId = id!!.dropLast(samayId.length)
        val s = id!!.drop(ragaId.length)
        if(s == samayId) {
            val list: java.util.ArrayList<Raga> = java.util.ArrayList<Raga>()
            Log.i("playButton", "id : " + id)
            for (raga: Raga in _ragaList.value!!) {

                if (raga.id == ragaId) {

                    raga.playButton = "ic_pause_black_24dp"
                } else {

                    raga.playButton = "ic_play_arrow_black_24dp"
                }
                list.add(raga)
            }
            _ragaList.value = null
            _ragaList.value = list
        }
    }
}