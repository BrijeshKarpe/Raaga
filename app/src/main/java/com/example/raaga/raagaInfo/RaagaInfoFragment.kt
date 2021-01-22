package com.example.raaga.raagaInfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.raaga.Constants
import com.example.raaga.IMainActivity
import com.example.raaga.MainActivity
import com.example.raaga.R
import com.example.raaga.databinding.FragmentRaagaInfoBinding
import com.example.raaga.media.MediaSeekBar
import com.example.raaga.media.client.MediaBrowserHelperCallback

@Suppress("DEPRECATION")
class RaagaInfoFragment : Fragment(),MediaBrowserHelperCallback{

    lateinit var viewModel:RaagaInfoFragmentViewModel
    lateinit var mediaSeekBar:MediaSeekBar
    lateinit var mSeekBarBroadcastReceiver: SeekbarBroadcastReceiver
    lateinit var mMainActivity:MainActivity
    var mIsPlaying:Boolean = false
    lateinit var mSelectedMedia:MediaMetadataCompat
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentRaagaInfoBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_raaga_info,container,false)
        val application = requireNotNull(this.activity).application
        val arguments = RaagaInfoFragmentArgs.fromBundle(requireArguments())
        val viewModelFactory = RaagaInfoFragmentViewModelFactory(arguments.ragaKey,arguments.samayKey,application)
        val mIMainActivity = activity as IMainActivity
        mMainActivity = activity as MainActivity
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(RaagaInfoFragmentViewModel::class.java)
        binding.raagaInfoViewModel = viewModel
        binding.setLifecycleOwner(this)
        viewModel.updateMainActivity(mIMainActivity)
        viewModel.play_pause_button_icon.observe(viewLifecycleOwner, Observer {

            binding.playPauseButton.setImageResource(it)
        })
        viewModel.mSelectedMediaItem.observe(viewLifecycleOwner, Observer {

            mSelectedMedia = it
        })
        viewModel.mIsPlaying.observe(viewLifecycleOwner, Observer {

            mIsPlaying = it
        })
        mediaSeekBar = binding.mediaSeekbar
        mediaSeekBar.setMediaController(mMainActivity.getMediaBrowserHelper().getMediaController())
        mMainActivity.getMediaBrowserHelper().setMediaBrowserHelperCallback(this)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState != null){

            mSelectedMedia = savedInstanceState.getParcelable("selected_media")!!
            viewModel.setUI(mSelectedMedia)
            viewModel.setIsPlaying(savedInstanceState.getBoolean("is_playing"))
        }
    }

    fun getMediaSeekbar(): MediaSeekBar {

        return mediaSeekBar
    }
    override fun onResume() {
        super.onResume()
        initSeekBarBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        getMediaSeekbar().disconnectController()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mSeekBarBroadcastReceiver)
    }

    fun initSeekBarBroadcastReceiver(){

        var intentFilter = IntentFilter()
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update))
        mSeekBarBroadcastReceiver = SeekbarBroadcastReceiver()
        requireActivity().registerReceiver(mSeekBarBroadcastReceiver,intentFilter)
    }
    inner class SeekbarBroadcastReceiver: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            val seekProgress = intent!!.getLongExtra(Constants.SEEK_BAR_PROGRESS, 0)
            val maxProgress = intent.getLongExtra(Constants.SEEK_BAR_MAX, 0)
            if(!getMediaSeekbar().isTracking){

                getMediaSeekbar().progress = seekProgress.toInt()
                getMediaSeekbar().max = maxProgress.toInt()
                viewModel.setTime(seekProgress,maxProgress)
            }
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

        if(metadata == null){

            return
        }

        viewModel.setUI(metadata)
        viewModel.setIsPlaying(mMainActivity.mIsPlaying)
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        mMainActivity.mIsPlaying = when{
            (state!!.state == PlaybackStateCompat.STATE_PLAYING) -> true
            else -> false
        }
        viewModel.setIsPlaying(mMainActivity.mIsPlaying)
    }

}
