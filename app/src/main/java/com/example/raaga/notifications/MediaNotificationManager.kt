package com.example.raaga.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.example.raaga.MainActivity
import com.example.raaga.R
import com.example.raaga.media.AudioPlayerService

class MediaNotificationManager(mediaService:AudioPlayerService) {

    var mMediaService:AudioPlayerService = mediaService
    var mNotificationManager:NotificationManager
    var mPlayAction: NotificationCompat.Action
    var mPauseAction: NotificationCompat.Action
    var mNextAction: NotificationCompat.Action
    var mPreviousAction: NotificationCompat.Action

    companion object{

        const val CHANNEL_ID:String = "com.example.raaga"
        const val REQUEST_CODE:Int = 101
        const val NOTIFICATION_ID:Int = 201
    }

    init {

        mNotificationManager = mMediaService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mPlayAction = NotificationCompat.Action(
            R.drawable.exo_controls_play,
            mMediaService.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,PlaybackStateCompat.ACTION_PLAY)
        )

        mPauseAction = NotificationCompat.Action(

            R.drawable.exo_controls_pause,
            mMediaService.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,PlaybackStateCompat.ACTION_PAUSE)
        )

        mNextAction = NotificationCompat.Action(

            R.drawable.exo_controls_next,
            mMediaService.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        )
        mPreviousAction = NotificationCompat.Action(

            R.drawable.exo_controls_previous,
            mMediaService.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        )
        mNotificationManager.cancelAll()
    }

    fun getNotificationManager(): NotificationManager {

        return mNotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){



            var name:CharSequence = "MediaSession"
            var description:String = "Media Session for the Media Player"
            var importance = NotificationManager.IMPORTANCE_LOW
            var mChannel:NotificationChannel = NotificationChannel(CHANNEL_ID,name,importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 100, 100, 100, 100, 100)
            mNotificationManager.createNotificationChannel(mChannel)
            Log.i(TAG,"createChannel : new notification channel created")

    }
    private fun isOOrHigher():Boolean{

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun buildNotification(@NonNull state:PlaybackStateCompat,
                          token:MediaSessionCompat.Token,
                          description:MediaDescriptionCompat,
                          bitmap: Bitmap?
                          ): Notification? {
        var isPlaying:Boolean = when{
            (state.state == PlaybackStateCompat.STATE_PLAYING) -> true
            else -> false
        }

        if(isOOrHigher()){

            createChannel()
        }
        var builder:NotificationCompat.Builder = NotificationCompat.Builder(mMediaService,CHANNEL_ID)
            builder.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0,1,2)
            )
            .setColor(ContextCompat.getColor(mMediaService, R.color.primaryColor))
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .setContentIntent(createContentIntent())
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setLargeIcon(description.iconBitmap)
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(mMediaService,PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if((state.actions.toInt() and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS.toInt()) != 0){

            builder.addAction(mPreviousAction)
        }
        builder.addAction(when{
            isPlaying -> mPauseAction
            else -> mPlayAction
        })
        if((state.actions.toInt() and PlaybackStateCompat.ACTION_SKIP_TO_NEXT.toInt()) != 0){

            builder.addAction(mNextAction)
        }
        return builder.build()
    }
    private fun createContentIntent():PendingIntent{

        var openUI:Intent = Intent(mMediaService,MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(mMediaService, REQUEST_CODE,openUI,PendingIntent.FLAG_CANCEL_CURRENT)
    }
}
private const val TAG = "MediaNotificationManage"