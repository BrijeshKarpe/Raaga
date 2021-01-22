package com.example.raaga

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.raaga.Constants.MEDIA_QUEUE_POSITION
import com.example.raaga.Constants.QUEUE_NEW_PLAYLIST
import com.example.raaga.data.Raga
import com.example.raaga.data.Samay
import com.example.raaga.data.SamayList
import com.example.raaga.databinding.ActivityMainBinding
import com.example.raaga.media.AudioPlayerService
import com.example.raaga.media.MyApplication
import com.example.raaga.media.client.MediaBrowserHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity(),IMainActivity {
    private lateinit var drawerLayout: DrawerLayout
    lateinit var mMediaBrowserHelper: MediaBrowserHelper
    lateinit var mMyPreferenceManager:MyPreferenceManager
    var mIsPlaying:Boolean = true
    var mMyApplication:MyApplication? = null
    var mOnAppOpen:Boolean = false
    var mWasConfigurationChanged:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMyApplication = MyApplication.mInstance
        //Log.i(TAG,"preference manager "+ MyPreferenceManager(this).getPlaylistId())
        mMyPreferenceManager = MyPreferenceManager(this)

        mMediaBrowserHelper = MediaBrowserHelper(this, AudioPlayerService::class.java)

            val binding: ActivityMainBinding = DataBindingUtil.setContentView<ActivityMainBinding>(
                this,
                R.layout.activity_main
            )
            drawerLayout = binding.drawerLayout
            val navController = this.findNavController(R.id.nav_host_fragment)
            NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
            NavigationUI.setupWithNavController(binding.navView, navController)
            navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, args: Bundle? ->
                if (nd.id == nc.graph.startDestination) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                } else {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }

    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart : called")
        mWasConfigurationChanged = getMyPreferenceManager().getOnStop()
        if(getMyPreferenceManager().getPlaylistId() != ""){

            prepareLastPlayedMedia()
        }else {
            mMediaBrowserHelper.onStart(mWasConfigurationChanged)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mWasConfigurationChanged = true
    }

    private fun prepareLastPlayedMedia() {

        val gson = Gson()
        val data: String? = getAssetJsonData(applicationContext)
        val type = object : TypeToken<SamayList>() {}.type
        val properties: SamayList = gson.fromJson(data, type)
        val playlist: ArrayList<MediaMetadataCompat> = ArrayList<MediaMetadataCompat>()
        val samay: Samay = properties.samayRagaList.get(Integer.parseInt(getMyPreferenceManager().getLastPlayedArtist()) - 1)
        for (raga: Raga in samay.raga){

            playlist.add(
                MediaMetadataCompat.Builder()
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        ("" + raga.id + "" + raga.sid + "")
                    )
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, raga.name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, raga.thaat)
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                        Uri.parse(raga.audio).toString()
                    )
                    .build()
            )
        }
        onFinishedGettingPreviousMedia(playlist)
    }

    private fun onFinishedGettingPreviousMedia(playlist: ArrayList<MediaMetadataCompat>) {

        mMyApplication!!.setMediaItems(playlist)
        mMediaBrowserHelper.onStart(mWasConfigurationChanged)
    }

    fun getMediaBrowserHelper(): MediaBrowserHelper {

        return mMediaBrowserHelper
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG,"onStop called")
        getMyPreferenceManager().setOnStop(true)
        mMediaBrowserHelper.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun getMyApplication(): MyApplication {
        return mMyApplication!!
    }

    override fun onMediaSelected(
        playlistId: String,
        mediaItem: MediaMetadataCompat,
        queuePosition: Int
    ) {

        val bundle = Bundle()
//        if(queuePosition != Integer.parseInt(getMyPreferenceManager().getLastPlayedMedia()!![0].toString()))
            bundle.putInt(MEDIA_QUEUE_POSITION, queuePosition)

        val currentPlaylistId:String = getMyPreferenceManager().getPlaylistId()!!
      //  val currentMediaId:String = getMyPreferenceManager().getLastPlayedMedia()!!
        Log.i(TAG,"last playlist id : "+currentPlaylistId+"Current Playlist id : "+playlistId)

            if (playlistId == currentPlaylistId) {

                mMediaBrowserHelper.getTransportControls().playFromMediaId(
                    mediaItem.description.mediaId,
                    bundle
                )

            } else {

                bundle.putBoolean(QUEUE_NEW_PLAYLIST, true)
                mMediaBrowserHelper.subscribeToNewPlaylist(playlistId,currentPlaylistId)
                mMediaBrowserHelper.getTransportControls().playFromMediaId(
                    mediaItem.description.mediaId,
                    bundle
                )
            }

        mOnAppOpen = true
    }

    override fun playPause() {

        if(mOnAppOpen) {
            if (mIsPlaying) {

                mMediaBrowserHelper.getTransportControls().pause()
            } else {

                mMediaBrowserHelper.getTransportControls().play()
            }
        }else{

            if(getMyPreferenceManager().getPlaylistId() != ""){

                onMediaSelected(
                    getMyPreferenceManager().getPlaylistId()!!,
                    mMyApplication!!.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()!!)!!,
                    getMyPreferenceManager().getQueuePosition()
                )
            }
            else{

                Toast.makeText(this,"Select Something to play",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getMyPreferenceManager(): MyPreferenceManager {

        return mMyPreferenceManager
    }

    override fun skipToNext() {

        mMediaBrowserHelper.getTransportControls().skipToNext()
    }

    override fun skipToPrevious() {


        mMediaBrowserHelper.getTransportControls().skipToPrevious()
    }

}
private const val TAG = "MainActivity"