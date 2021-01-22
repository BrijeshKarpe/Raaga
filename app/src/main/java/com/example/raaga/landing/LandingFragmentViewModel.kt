package com.example.raaga.landing

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
import java.util.*


class LandingFragmentViewModel(application: Application) : AndroidViewModel(application){

    lateinit var mSelectedMedia: MediaMetadataCompat
    lateinit var mIMainActivity: IMainActivity

    var mCurrentPlaylist:ArrayList<MediaMetadataCompat> = ArrayList<MediaMetadataCompat>()
    lateinit var samay:Samay
    private var samayList = MutableLiveData<SamayList>()
    val _samayList : LiveData<SamayList>
        get() = samayList
    private var _ragaList = MutableLiveData<List<Raga>>()
    val ragaList : LiveData<List<Raga>>
    get() = _ragaList
    var currentRaga = MutableLiveData<String>()
    var currentSamay:String = ""
    var timeFrom = MutableLiveData<String>()
    var timeTo = MutableLiveData<String>()
    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main+viewModelJob)
    private var applicationContext: Context
    init {
        initializeSamay()
        //_ragaList.value = listOf(Raga("one","two","three"))
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

    private  fun createPlaylist(samay: Samay):ArrayList<MediaMetadataCompat>{

            Log.i("LandingFragmentViewModel","createPlaylist Called")
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

    private suspend fun getSamayFromJSON(): SamayList? {

        return withContext(Dispatchers.IO){
            val gson = Gson()
            val data: String? = getAssetJsonData(applicationContext)
            val type = object : TypeToken<SamayList>() {}.type

            val properties: SamayList = gson.fromJson(data, type)
            properties
        }
    }
    fun updateUI(){

        val current_hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val current_raga = when{

            (current_hour in 0..1) -> 10
            (current_hour in 2..3) -> 11
            (current_hour in 4..5) -> 0
            (current_hour in 6..7) -> 1
            (current_hour in 8..9) -> 2
            (current_hour in 10..11) -> 3
            (current_hour in 12..13) -> 4
            (current_hour in 14..15) -> 5
            (current_hour in 16..17) -> 6
            (current_hour in 18..19) -> 7
            (current_hour in 20..21) -> 8
            (current_hour in 22..23) -> 9
            else -> 4
        }

        samay = ((samayList.value?.samayRagaList)?.get(current_raga)!!)
        currentRaga.value = (samay.name)?.capitalize()
        timeFrom.value = samay.time_from
        timeTo.value = samay.time_to
        _ragaList.value = samay.raga
        currentSamay = samay.id
        val last:MediaMetadataCompat? = mIMainActivity.getMyApplication().getMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia()!!)
        if(last != null){setPlayButton(last)}
    }
    private val _onClickRagaInfo = MutableLiveData<String>()
    val onclickRagaInfo : LiveData<String>
        get() = _onClickRagaInfo
    fun onClickRaga(id:String?) {
        _onClickRagaInfo.value = id
            //if(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia() != (id+currentSamay)) {
            mCurrentPlaylist = createPlaylist(samay)
            mIMainActivity.getMyApplication().setMediaItems(mCurrentPlaylist)
            //}
            mSelectedMedia = mCurrentPlaylist[Integer.parseInt(id) - 1]

            mIMainActivity.onMediaSelected(samay.id, mSelectedMedia, Integer.parseInt(id) - 1)

            saveLastPlayedSongProperties()

    }
    fun onNavigateRagaInfo(){

        _onClickRagaInfo.value = null
    }
    fun updateMainActivity(activity: IMainActivity){

        mIMainActivity = activity

    }
    fun saveLastPlayedSongProperties(){

        mIMainActivity.getMyPreferenceManager().setPlaylistId(samay.id)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedArtist(samay.id)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedCategory(currentRaga.value)
        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.description.mediaId)
    }

    fun setPlayButton(mediaItem: MediaMetadataCompat) {

        val id = mediaItem.description.mediaId
        val samayId = samay.id
        val ragaId = id!!.dropLast(samayId.length)
        val s = id!!.drop(ragaId.length)
        val list: ArrayList<Raga> = ArrayList<Raga>()
        if(s == samayId) {

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
        }else{

            for (raga: Raga in _ragaList.value!!) {

                raga.playButton = "ic_play_arrow_black_24dp"
                list.add(raga)
            }
            _ragaList.value = null
            _ragaList.value = list
        }
    }
    private val _mShow = MutableLiveData<Boolean>()
    val mShow : LiveData<Boolean>
        get() = _mShow
    fun showCurrentPlaylist(){

        _mShow.value = true
    }

    fun onNavigateToRaagasFragment() {

        _mShow.value = false
    }
}