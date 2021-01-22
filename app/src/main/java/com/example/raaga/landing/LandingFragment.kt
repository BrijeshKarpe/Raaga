package com.example.raaga.landing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.raaga.IMainActivity
import com.example.raaga.MainActivity
import com.example.raaga.R
import com.example.raaga.databinding.FragmentLandingBinding


@Suppress("DEPRECATION")
class LandingFragment : Fragment() {

    lateinit var mMainActivity: MainActivity
    lateinit var viewModel: LandingFragmentViewModel
    lateinit var mUpdateUiBroadcastReceiver:UpdateUIBroadcastReceiver
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val binding:FragmentLandingBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_landing,container,false)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = LandinFragmentViewModelFactory(application)
        viewModel = ViewModelProviders.of(this,viewModelFactory).get(LandingFragmentViewModel::class.java)
       // var x = requireActivity().intent.getStringExtra("RaagaInfoFragment")
        mMainActivity = activity as MainActivity
        binding.landingFragmentViewModel = viewModel
        binding.setLifecycleOwner(this)
        setHasOptionsMenu(true)
        val adapter = RagaListAdapter(RagaListener { id ->
            //Toast.makeText(context,"$id",Toast.LENGTH_LONG).show()
            viewModel.onClickRaga(id!!)
        })

        binding.ragaList.adapter = adapter
        viewModel.ragaList.observe(viewLifecycleOwner, Observer {

            it.let {

                adapter.submitList(it)
            }
        })
        viewModel._samayList.observe(viewLifecycleOwner, Observer {

            it.let {
                viewModel.updateMainActivity(activity as IMainActivity)
                viewModel.updateUI()
            }
        })
        viewModel.onclickRagaInfo.observe(viewLifecycleOwner, Observer {raga ->
            raga?.let {

                this.findNavController().navigate(LandingFragmentDirections.actionLandingFragmentToRaagaInfoFragment(raga,viewModel.currentSamay))
                viewModel.onNavigateRagaInfo()
            }
        })

        viewModel.mShow.observe(viewLifecycleOwner, Observer {
            if (it) {
                val samay = mMainActivity.getMyPreferenceManager().getPlaylistId().toString()
                if (samay != "") {
                    this.findNavController().navigate(
                        LandingFragmentDirections.actionLandingFragmentToRaagasFragment(samay)
                    )
                    viewModel.onNavigateToRaagasFragment()
                } else {

                    Toast.makeText(context,"No Current Playlist",Toast.LENGTH_SHORT).show()
                }
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
        requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        initUpdateUiBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        if(mUpdateUiBroadcastReceiver != null){

            requireActivity().unregisterReceiver(mUpdateUiBroadcastReceiver)
        }
    }

    fun initUpdateUiBroadcastReceiver(){

        var intentFilter = IntentFilter()
        intentFilter.addAction(getString(R.string.broadcast_update_ui))
        mUpdateUiBroadcastReceiver = UpdateUIBroadcastReceiver()
        requireActivity().registerReceiver(mUpdateUiBroadcastReceiver,intentFilter)
    }

    inner class UpdateUIBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(p0: Context?, intent: Intent?) {

            var mediaID: String? = intent!!.getStringExtra(getString(R.string.broadcast_new_media_id))

            viewModel.setPlayButton(mMainActivity.mMyApplication!!.getMediaItem(mediaID!!)!!)
        }
    }
}