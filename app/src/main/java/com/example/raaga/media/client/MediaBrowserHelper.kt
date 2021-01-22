package com.example.raaga.media.client

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat

class MediaBrowserHelper(context: Context, serviceClass: Class<out MediaBrowserServiceCompat?>) {

    var mContext:Context
    var mMediaBrowserServiceClass:Class<out MediaBrowserServiceCompat?>
    var mMediaControllerCallback:MediaControllerCallback
    var mMediaBrowserHelperCallback:MediaBrowserHelperCallback? = null
    var mMediaBrowser:MediaBrowserCompat? = null
    var mMediaController:MediaControllerCompat? = null
    var mMediaBrowserConnectionCallback: MediaBrowserConnectionCallback? = null
    var mMediaBrowserSubscriptionCallback: MediaBrowserSubscriptionCallback? = null
    var mWasConfigurationChanged:Boolean = false
    var mIsSubscribedToNewPlaylist = false
    init{
        Log.i(TAG,"MediaBrowserHelper : initialized")
        mContext = context

        mMediaBrowserServiceClass = serviceClass
        mMediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
        mMediaBrowserSubscriptionCallback = MediaBrowserSubscriptionCallback()
        mMediaControllerCallback = MediaControllerCallback()

    }

    inner class MediaControllerCallback: MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

            Log.i(TAG,"OnPlaybackStateChanged Called")
            if(mMediaBrowserHelperCallback != null) {
                mMediaBrowserHelperCallback!!.onPlaybackStateChanged(state!!)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i(TAG, "OnMetadataChanged Called")

            if (mMediaBrowserHelperCallback != null) {
                mMediaBrowserHelperCallback!!.onMetadataChanged(metadata!!)
            }
        }
    }

    fun setMediaBrowserHelperCallback(mediaBrowserHelperCallback: MediaBrowserHelperCallback){

        mMediaBrowserHelperCallback = mediaBrowserHelperCallback
    }

    fun onStart(wasConfigurationChanged:Boolean){
        mWasConfigurationChanged = wasConfigurationChanged

        if(mMediaBrowser == null){

            mMediaBrowser = MediaBrowserCompat(
                mContext,
                ComponentName(mContext, mMediaBrowserServiceClass),
                mMediaBrowserConnectionCallback,
                null
            )
            mMediaBrowser!!.connect()
        }
    }
    fun onStop(){

        if(mMediaController != null){

            mMediaController!!.unregisterCallback(mMediaControllerCallback)
            mMediaController = null
        }
        if(mMediaBrowser != null && mMediaBrowser!!.isConnected){

            mMediaBrowser!!.disconnect()
            mMediaBrowser = null
        }
    }
    fun getTransportControls():MediaControllerCompat.TransportControls {

        if(mMediaController == null){

            throw IllegalStateException("Media Controller is null!")
        }
        return mMediaController!!.transportControls
    }

    fun subscribeToNewPlaylist(playlistId: String, currentPlaylistId: String){

        mMediaBrowser!!.unsubscribe(currentPlaylistId)
        mIsSubscribedToNewPlaylist = true
        mMediaBrowser!!.subscribe(playlistId, mMediaBrowserSubscriptionCallback!!)
        Log.i(TAG,"Subscribing to playlist : "+playlistId)
    }

    inner class MediaBrowserConnectionCallback: MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {

            //try {

                mMediaController = MediaControllerCompat(mContext,mMediaBrowser!!.sessionToken)
                Log.i(TAG,"MediaControllerCallback : "+mMediaControllerCallback + "Media controller : "+mMediaController)
                mMediaController!!.registerCallback(mMediaControllerCallback)

//            }catch (e: RemoteException){
//
//                Log.i(TAG,"onConnected : connection problem : "+e.toString())
//            }
            mMediaBrowser!!.subscribe(mMediaBrowser!!.root,mMediaBrowserSubscriptionCallback!!)

        }
    }

    fun getMediaController(): MediaControllerCompat? {

        return mMediaController
    }
    inner class MediaBrowserSubscriptionCallback: MediaBrowserCompat.SubscriptionCallback(){

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Log.i(TAG,"onChildrenLoaded : Called")
            if(!mWasConfigurationChanged){

                for (mediaItem in children){

                    mMediaController!!.addQueueItem(mediaItem.description)
                }
            }else if(mIsSubscribedToNewPlaylist){

                for (mediaItem in children){

                    mMediaController!!.addQueueItem(mediaItem.description)
                }
                mIsSubscribedToNewPlaylist = false
            }

        }
    }

}
private const val TAG = "MediaBrowserHelper"
