package com.example.raaga.media.client

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat

interface MediaBrowserHelperCallback {

    fun onMetadataChanged(metadata: MediaMetadataCompat?)
    fun onPlaybackStateChanged(state: PlaybackStateCompat?)
}