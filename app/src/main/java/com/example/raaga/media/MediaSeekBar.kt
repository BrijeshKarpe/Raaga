package com.example.raaga.media


import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar


class MediaSeekBar : AppCompatSeekBar {
    private var mMediaController: MediaControllerCompat? = null
    var isTracking = false
        private set
    private val mOnSeekBarChangeListener: OnSeekBarChangeListener =
        object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isTracking = false
                mMediaController!!.transportControls.seekTo(progress.toLong())
            }
        }

    constructor(context: Context?) : super(context!!) {
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        super.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
    }

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener) {
        // Prohibit adding seek listeners to this subclass.
        throw UnsupportedOperationException("Cannot add listeners to a MediaSeekBar")
    }

    fun setMediaController(mediaController: MediaControllerCompat?) {
        mMediaController = mediaController
    }

    fun disconnectController() {
        if (mMediaController != null) {
            mMediaController = null
        }
    }

    companion object {
        private const val TAG = "MediaSeekBar"
    }
}
