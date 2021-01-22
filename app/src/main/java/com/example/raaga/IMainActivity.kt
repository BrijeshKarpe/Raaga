package com.example.raaga

import android.support.v4.media.MediaMetadataCompat
import com.example.raaga.media.MyApplication

interface IMainActivity {

    fun getMyApplication(): MyApplication
    fun onMediaSelected(playlistId:String,mediaItem:MediaMetadataCompat,queuePosition:Int)
    fun playPause()
    fun getMyPreferenceManager():MyPreferenceManager
    fun skipToNext()
    fun skipToPrevious()
}