package com.example.raaga

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.provider.MediaStore.Audio.Playlists.Members.PLAYLIST_ID
import android.util.Log
import com.example.raaga.Constants.LAST_ARTIST
import com.example.raaga.Constants.LAST_ARTIST_IMAGE
import com.example.raaga.Constants.LAST_CATEGORY
import com.example.raaga.Constants.MEDIA_QUEUE_POSITION
import com.example.raaga.Constants.NOW_PLAYING


class MyPreferenceManager(context: Context) {

    var mPreferences:SharedPreferences

    //    fun setPlaylistId(playlistId:String){
//
//        var editor:SharedPreferences.Editor = mPreferences.edit()
//        editor.putString(PLAYLIST_ID,playlistId)
//        editor.apply()
//    }
//
    init {

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        mPreferences.edit().clear()
    }
    fun getPlaylistId(): String? {

        return mPreferences.getString(PLAYLIST_ID,"")
    }

     fun setPlaylistId(playlistId: String?) {
         Log.i(TAG,"setPlaylistId : called " + playlistId)
        val editor = mPreferences.edit()
        editor.putString(PLAYLIST_ID, playlistId)
        editor.apply()
    }
    fun saveQueuePosition(position: Int) {
        Log.d(TAG, "saveQueuePosition: SAVING QUEUE INDEX: $position")
        val editor = mPreferences.edit()
        editor.putInt(MEDIA_QUEUE_POSITION, position)
        editor.apply()
    }
    fun setOnStop(b: Boolean) {

        mPreferences.edit().putBoolean("stopped",b).apply()
    }
    fun getOnStop(): Boolean {

        return mPreferences.getBoolean("stopped",false)
    }
    fun getQueuePosition(): Int {
        return mPreferences.getInt(MEDIA_QUEUE_POSITION, -1)
    }

    fun saveLastPlayedArtistImage(url: String?) {
        val editor = mPreferences.edit()
        editor.putString(LAST_ARTIST_IMAGE, url)
        editor.apply()
    }

    fun getLastPlayedArtistImage(): String? {
        return mPreferences.getString(LAST_ARTIST_IMAGE, "")
    }

    fun getLastPlayedArtist(): String? {
        return mPreferences.getString(LAST_ARTIST, "")
    }

    fun getLastCategory(): String? {
        return mPreferences.getString(LAST_CATEGORY, "")
    }

    fun saveLastPlayedMedia(mediaId: String?) {
        val editor = mPreferences.edit()
        editor.putString(NOW_PLAYING, mediaId)
        editor.apply()
    }

    fun getLastPlayedMedia(): String? {
        return mPreferences.getString(NOW_PLAYING, "")
    }

    fun saveLastPlayedCategory(category: String?) {
        val editor = mPreferences.edit()
        editor.putString(LAST_CATEGORY, category)
        editor.apply()
    }

    fun saveLastPlayedArtist(artist: String?) {
        val editor = mPreferences.edit()
        editor.putString(LAST_ARTIST, artist)
        editor.apply()
    }
}
private const val TAG = "MyPreferenceManager"