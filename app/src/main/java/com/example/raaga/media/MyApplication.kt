package com.example.raaga.media

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

class MyApplication : Application(){


    val mMediaItems:ArrayList<MediaBrowserCompat.MediaItem> = ArrayList()
    val mTreeMap:TreeMap<String,MediaMetadataCompat> = TreeMap()
    companion object{

        val mInstance = MyApplication()
    }

    fun setMediaItems(mediaItems:ArrayList<MediaMetadataCompat>){

        mMediaItems.clear()
        Log.i("MyApplication","SetMediaItems called "+mediaItems.size)
        for(item:MediaMetadataCompat in mediaItems){

            mMediaItems.add(
                MediaBrowserCompat.MediaItem(item.description,MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            )
            mTreeMap[item.description.mediaId!!] = item
        }
        //Log.i("MyApplication","SetMediaItems called "+ mTreeMap["1"])
    }

    fun getMediaItems():ArrayList<MediaBrowserCompat.MediaItem>{

        return mMediaItems
    }

    fun getTreeMap():TreeMap<String,MediaMetadataCompat>{

        return mTreeMap
    }

    fun getMediaItem(mediaId:String): MediaMetadataCompat? {
        Log.i("MyApplication","getMediaItem called "+ mTreeMap[mediaId]+" "+mediaId+" "+mTreeMap.size)
        if(mTreeMap != null)
        {return mTreeMap[mediaId]}
        return null
    }

}
private const val TAG = "MyApplication"