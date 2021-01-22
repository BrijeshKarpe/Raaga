package com.example.raaga.media.players

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MediaPlayerAdapter(context: Context, playbackInfoListener: PlaybackInfoListener) : PlayerAdapter(
    context
){

    var mContext: Context
    var mCurrentMedia:MediaMetadataCompat? = null
    var isCurrentMediaPlayedToCompletion: Boolean = false
    var mState:Int = 0
    var mStartTime:Long = 0
    var mPlaybackInfoListener:PlaybackInfoListener
    //ExoPlayer variables

    var mExoPlayer: SimpleExoPlayer? = null
    lateinit var mTrackSelector: TrackSelector
    lateinit var mRenderer: DefaultRenderersFactory
    lateinit var mDataSourceFactory : DataSource.Factory
    var mExoplayerEventListener:ExoPlayerEventListener? = null
    //------
    init {

        mContext = context
        mPlaybackInfoListener = playbackInfoListener
    }
    private fun initializeExoPlayer(){

        if (mExoPlayer == null){

            mTrackSelector = DefaultTrackSelector()
            mRenderer = DefaultRenderersFactory(mContext)
            mDataSourceFactory = DefaultDataSourceFactory(
                mContext, Util.getUserAgent(
                    mContext,
                    "Raaga"
                )
            )
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                mContext,
                mRenderer,
                mTrackSelector,
                DefaultLoadControl()
            )
            if(mExoplayerEventListener == null){

                mExoplayerEventListener = ExoPlayerEventListener()
            }
            mExoPlayer!!.addListener(mExoplayerEventListener!!)
        }
    }

    private fun release(){

        if(mExoPlayer != null){

            mExoPlayer?.release()
            mExoPlayer = null
        }
    }

    override fun onPlay() {
        if(mExoPlayer != null && !mExoPlayer!!.playWhenReady){

            mExoPlayer!!.playWhenReady = true
            setNewState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onPause() {
        if(mExoPlayer != null && mExoPlayer!!.playWhenReady){

            mExoPlayer!!.playWhenReady = false
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }
    var x = true
    //val mMyPreferenceManager: MyPreferenceManager = MyPreferenceManager(context)
    override fun playFromMedia(metadata: MediaMetadataCompat?) {

        //if(!MyPreferenceManager(mApplicationContext).getOnStop())
        if(x) {
            startTrackingPlayback()
            x=false
        }
        playFile(metadata)
        //mMyPreferenceManager.saveLastPlayedArtistImage(metadata.description.mediaId)
    }
    override val currentMedia: MediaMetadataCompat?
        get() = mCurrentMedia
    override val isPlaying: Boolean
        get() = when{
            mExoPlayer != null -> mExoPlayer!!.playWhenReady
            else -> false
        }

    override fun onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    override fun seekTo(position: Long) {
        if(mExoPlayer != null){

            mExoPlayer!!.seekTo(position)
            setNewState(mState)
        }
    }

    override fun setVolume(volume: Float) {
        if(mExoPlayer != null){

            mExoPlayer!!.volume = volume
        }
    }

    private fun playFile(metadata: MediaMetadataCompat?) {

        var mediaId = metadata?.description?.mediaId
        var mediaChanged = (mCurrentMedia == null) || (mediaId != mCurrentMedia?.description?.mediaId)
        if(isCurrentMediaPlayedToCompletion){

            mediaChanged = true
            isCurrentMediaPlayedToCompletion = false
        }
        if(!mediaChanged){

            if(!isPlaying){

                play()
            }
            return
        }else{

            release()
        }
        mCurrentMedia = metadata
        initializeExoPlayer()
        //Log.i(TAG,metadata.toString())
        var audioSource = ExtractorMediaSource.Factory(mDataSourceFactory)
            .createMediaSource(Uri.parse(mCurrentMedia?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)))
        mExoPlayer?.prepare(audioSource)
        play()
    }

    open fun startTrackingPlayback() {

         val handler = Handler(Looper.getMainLooper())
         val runnable: Runnable = object : Runnable {
            override fun run() {
                if (mExoPlayer != null) {
                    if (mExoPlayer!!.contentPosition >= mExoPlayer!!.duration
                        && mExoPlayer!!.duration > 0
                    ) {
                        mPlaybackInfoListener.onPlaybackComplete()
                    }
                    if (isPlaying) {
                        mPlaybackInfoListener.seekTo(
                            mExoPlayer!!.contentPosition, mExoPlayer!!.duration
                        )
                        handler.postDelayed(this, 100L)
                    }

                }
            }
        }
        handler.postDelayed(runnable, 100L)
    }

    fun setNewState(@PlaybackStateCompat.State newPlayerState: Int){

        mState = newPlayerState
        if(mState == PlaybackStateCompat.STATE_STOPPED){

            isCurrentMediaPlayedToCompletion = true
        }
        val reportPosition:Long = when{

            mExoPlayer == null -> 0
            else -> mExoPlayer!!.currentPosition
        }
        publishStateBuilder(reportPosition)
    }
    fun publishStateBuilder(reportPosition: Long){

        val stateBuilder:PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(getAvailableActions())
        stateBuilder.setState(
            mState,
            reportPosition,
            1.0f,
            SystemClock.elapsedRealtime()
        )
        mPlaybackInfoListener.onPlaybackStateChanged(stateBuilder.build())
        mPlaybackInfoListener.updateUI(mCurrentMedia!!.description.mediaId!!)
    }
    inner class ExoPlayerEventListener:Player.EventListener{

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when(playbackState){
                Player.STATE_ENDED -> setNewState(PlaybackStateCompat.STATE_PAUSED)
                Player.STATE_IDLE -> setNewState(PlaybackStateCompat.STATE_PAUSED)
                Player.STATE_BUFFERING -> mStartTime = System.currentTimeMillis()
                Player.STATE_READY -> Log.d(
                    TAG,
                    "onPlayerStateChanged: TIME ELAPSED: " + (System.currentTimeMillis() - mStartTime)
                )
                else -> Log.e(TAG, "Something Went Wrong")
            }
        }
    }

    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long {
        var actions = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        actions = when (mState) {
            PlaybackStateCompat.STATE_STOPPED -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PAUSE)
            PlaybackStateCompat.STATE_PLAYING -> actions or (PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_SEEK_TO)
            PlaybackStateCompat.STATE_PAUSED -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_STOP)
            else -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE)
        }
        return actions
    }
}