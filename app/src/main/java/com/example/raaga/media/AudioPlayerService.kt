package com.example.raaga.media

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.raaga.Constants.MEDIA_QUEUE_POSITION
import com.example.raaga.Constants.QUEUE_NEW_PLAYLIST
import com.example.raaga.Constants.SEEK_BAR_MAX
import com.example.raaga.Constants.SEEK_BAR_PROGRESS
import com.example.raaga.MyPreferenceManager
import com.example.raaga.R
import com.example.raaga.media.players.MediaPlayerAdapter
import com.example.raaga.media.players.PlaybackInfoListener
import com.example.raaga.media.players.PlayerAdapter
import com.example.raaga.notifications.MediaNotificationManager

class AudioPlayerService : MediaBrowserServiceCompat() {

    lateinit var mSession:MediaSessionCompat
    lateinit var mPlayback:PlayerAdapter
    private lateinit var myApplication:MyApplication
    lateinit var mMediaNotificationManager:MediaNotificationManager
    var mIsServiceStarted:Boolean = false
    lateinit var mPreferenceManager:MyPreferenceManager
    override fun onCreate() {
        super.onCreate()
        myApplication = MyApplication.mInstance
        mPreferenceManager = MyPreferenceManager(this)

        mSession = MediaSessionCompat(this, TAG)
        mSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        )
        mSession.setCallback(MediaSessionCallback())
        sessionToken = mSession.sessionToken
        mPlayback = MediaPlayerAdapter(this, MediaPlayerListener())
        mMediaNotificationManager = MediaNotificationManager(this)
    }
    override fun onTaskRemoved(rootIntent: Intent?) {

        super.onTaskRemoved(rootIntent)
        mPlayback.stop()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSession.release()

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        if(clientPackageName == application.packageName) {
            return BrowserRoot("Some_Playlist", null)
        }
        return BrowserRoot("empty_media",null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == "empty_media"){

            result.sendResult(null)
            return
        }
        Log.i(TAG,"onLoadChildren : Called")
        result.sendResult(myApplication.getMediaItems())
    }
    inner class MediaSessionCallback:MediaSessionCompat.Callback(){

        val mPlayList:ArrayList<MediaSessionCompat.QueueItem> = ArrayList<MediaSessionCompat.QueueItem>()
        var mQueueIndex:Int = -1
        var mPreparedMedia:MediaMetadataCompat? = null
        override fun onPrepare() {

            if(mQueueIndex < 0 && mPlayList.isEmpty()){

                return
            }


            var mediaId:String = mPlayList.get(mQueueIndex).description.mediaId!!
            mPreparedMedia = myApplication.getMediaItem(mediaId)
            mSession.setMetadata(mPreparedMedia)

            if(!mSession.isActive) {
                mSession.isActive = true
            }
        }

        override fun onPlay() {

            if(!isReadyToPlay()){

                return
            }
            if(mPreparedMedia == null){

                onPrepare()
            }
            Log.i(TAG, "Playing media : " + mPreparedMedia!!.description.title)
            mPlayback.playFromMedia(mPreparedMedia)

            mPreferenceManager.saveQueuePosition(mQueueIndex)
            mPreferenceManager.saveLastPlayedMedia(mPreparedMedia!!.description.mediaId)
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            if(extras!!.getBoolean(QUEUE_NEW_PLAYLIST, false)) {

                resetPlaylist()
            }
            mPreparedMedia = myApplication.getMediaItem(mediaId)
            Log.i(TAG, "Playing media : " + mPreparedMedia!!.description.title)
            mSession.setMetadata(mPreparedMedia)
            if (!mSession.isActive){

                mSession.isActive = true
            }

                mPlayback.playFromMedia(mPreparedMedia)

            var queueIndex:Int = extras!!.getInt(MEDIA_QUEUE_POSITION, -1)

            if(queueIndex == -1){

                mQueueIndex++
            }else{

                mQueueIndex = queueIndex
            }

            mPreferenceManager.saveQueuePosition(mQueueIndex)
            mPreferenceManager.saveLastPlayedMedia(mPreparedMedia!!.description.mediaId)
        }

        override fun onPause() {
            mPlayback.pause()
        }

        override fun onSkipToNext() {

            mQueueIndex = (++mQueueIndex % mPlayList.size)
            mPreparedMedia = null

            onPlay()
        }

        override fun onSkipToPrevious() {

            mQueueIndex = when{

                mQueueIndex > 0 -> mQueueIndex - 1
                else -> mPlayList.size - 1
            }

            mPreparedMedia = null
            onPlay()
        }

        override fun onStop() {
            mPlayback.stop()
            mSession.isActive = false
        }

        override fun onSeekTo(pos: Long) {
            mPlayback.seekTo(pos)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            Log.i(TAG, "onAddQueueItem Called" + mPlayList.size)
            mPlayList.add(
                MediaSessionCompat.QueueItem(
                    description,
                    description.hashCode().toLong()
                )
            )
            mQueueIndex = when(mQueueIndex){
                -1 -> 0
                else -> mQueueIndex
            }

            mSession.setQueue(mPlayList)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {

            mPlayList.remove(
                MediaSessionCompat.QueueItem(
                    description,
                    description.hashCode().toLong()
                )
            )
            mQueueIndex = when{
                mPlayList.isEmpty() -> -1
                else -> mQueueIndex
            }

            mSession.setQueue(mPlayList)
        }
        private fun isReadyToPlay():Boolean{
            return mPlayList.isNotEmpty()
        }
        private fun resetPlaylist(){

            mPlayList.clear()
            mQueueIndex = -1

        }
    }
    inner class MediaPlayerListener : PlaybackInfoListener{

        var mServiceManager:ServiceManager
        init {

            mServiceManager = ServiceManager()
        }
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

            mSession.setPlaybackState(state)
            when(state!!.state){

                PlaybackStateCompat.STATE_PLAYING -> mServiceManager.displayNotification(state)
                PlaybackStateCompat.STATE_PAUSED -> mServiceManager.displayNotification(state)
                PlaybackStateCompat.STATE_STOPPED -> mServiceManager.moveServiceOutOfStartedState()
            }
        }

        override fun seekTo(progress: Long, max: Long) {

            var intent:Intent = Intent()
            intent.action = getString(R.string.broadcast_seekbar_update)
            intent.putExtra(SEEK_BAR_PROGRESS, progress)
            intent.putExtra(SEEK_BAR_MAX, max)
            sendBroadcast(intent)
        }

        override fun onPlaybackComplete() {
            //Log.i(TAG,"onPlaybackComplete : Skipping to next; session : "+mSession+" controller : "+mSession.controller)
            mSession.controller.transportControls.skipToNext()
        }

        override fun updateUI(newMediaID: String) {

            val intent:Intent = Intent()
            intent.action = getString(R.string.broadcast_update_ui)
            intent.putExtra(getString(R.string.broadcast_new_media_id),newMediaID)
            sendBroadcast(intent)
        }
    }
    inner class ServiceManager{

        lateinit var mState:PlaybackStateCompat
        fun displayNotification(state: PlaybackStateCompat){
            mState = state
            var notification: Notification? = null
            when(mState.state){

                PlaybackStateCompat.STATE_PLAYING -> {
                    notification = mMediaNotificationManager.buildNotification(
                        mState,
                        sessionToken!!,
                        mPlayback.currentMedia!!.description,
                        null
                    )
                    if (!mIsServiceStarted) {
                        ContextCompat.startForegroundService(
                            this@AudioPlayerService,
                            Intent(this@AudioPlayerService, AudioPlayerService::class.java)
                        )
                        mIsServiceStarted = true
                    }
                    startForeground(MediaNotificationManager.NOTIFICATION_ID,notification)
                }
                PlaybackStateCompat.STATE_PAUSED ->{

                    stopForeground(false)
                    notification = mMediaNotificationManager.buildNotification(
                        mState,
                        sessionToken!!,
                        mPlayback.currentMedia!!.description,
                        null
                    )
                    mMediaNotificationManager.getNotificationManager().notify(MediaNotificationManager.NOTIFICATION_ID,notification)
                }

            }
        }
        fun moveServiceOutOfStartedState(){

            stopForeground(true)
            stopSelf()
            MyPreferenceManager(applicationContext).setOnStop(false)
            mIsServiceStarted = false

        }
    }
}

private const val TAG = "AudioPlayerService"