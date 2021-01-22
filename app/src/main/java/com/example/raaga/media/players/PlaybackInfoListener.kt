package com.example.raaga.media.players

import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackInfoListener {

    fun onPlaybackStateChanged(state: PlaybackStateCompat?)
    fun seekTo(progress:Long,max:Long)
    fun onPlaybackComplete()
    fun updateUI(newMediaID:String)
}