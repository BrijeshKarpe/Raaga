package com.example.raaga.raagaInfo

import android.app.Application
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raaga.IMainActivity
import com.example.raaga.R

class RaagaInfoFragmentViewModel(
    private val raga_key:String,
    private val samay_key:String,
    application:Application
):AndroidViewModel(application){

    private val _mIsPlaying = MutableLiveData<Boolean>()
    val mIsPlaying:LiveData<Boolean>
        get() =_mIsPlaying
    private val _mSelectedMediaItem = MutableLiveData<MediaMetadataCompat>()
    val mSelectedMediaItem : LiveData<MediaMetadataCompat>
        get() = _mSelectedMediaItem
    private val _play_pause_button_icon = MutableLiveData<Int>()
    val play_pause_button_icon : LiveData<Int>
        get() = _play_pause_button_icon
    lateinit var mIMainActivity:IMainActivity
    private val _raga_id = MutableLiveData<String>()
    val raga_id : LiveData<String>
        get() = _raga_id
    private val _samay_id = MutableLiveData<String>()
    val samay_id : LiveData<String>
        get() = _samay_id
    init {

        _raga_id.value = raga_key
        _samay_id.value = samay_key
    }
    fun updateMainActivity(activity: IMainActivity){

        mIMainActivity = activity

    }

    fun playPause(){

        mIMainActivity.playPause()
    }
    fun skipToNext(){

        mIMainActivity.skipToNext()
    }
    fun skipToPrevious(){

        mIMainActivity.skipToPrevious()
    }
    fun setUI(metadata: MediaMetadataCompat){
        _mSelectedMediaItem.value = metadata
        _raga_id.value = metadata.description.title.toString()
        _samay_id.value = metadata.description.subtitle.toString()
        _play_pause_button_icon.value = R.drawable.exo_controls_play
    }

    fun setIsPlaying(isPlaying:Boolean){
        _mIsPlaying.value = isPlaying
        if(isPlaying){

            _play_pause_button_icon.value = R.drawable.exo_controls_pause
        }else{

            _play_pause_button_icon.value = R.drawable.exo_controls_play
        }
    }

    private val _mSeekProgress = MutableLiveData<String>()
    val mSeekProgress:LiveData<String>
        get() = _mSeekProgress

    private val _mSeekMax = MutableLiveData<String>()
    val mSeekMax:LiveData<String>
        get() = _mSeekMax

    fun setTime(seekProgress: Long, maxProgress: Long) {

        _mSeekProgress.value = timestampToMSS(getApplication(),seekProgress)
        _mSeekMax.value = timestampToMSS(getApplication(),maxProgress)
    }
    fun timestampToMSS(context: Context, position: Long): String {
        val totalSeconds = Math.floor(position / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (position < 0) context.getString(R.string.duration_unknown)
        else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
    }
}