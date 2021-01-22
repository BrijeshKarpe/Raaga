package com.example.raaga.raagas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.raaga.IMainActivity
import com.example.raaga.MainActivity
import com.example.raaga.R
import com.example.raaga.databinding.FragmentRaagasBinding

@Suppress("DEPRECATION")
class RaagasFragment : Fragment() {
    lateinit var viewModel: RaagasFragmentViewModel
    lateinit var mMainActivity:MainActivity
    lateinit var mUpdateUiBroadcastReceiver: UpdateUIBroadcastReceiver
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding:FragmentRaagasBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_raagas,container,false)
        val application = requireNotNull(this.activity).application
        val arguments = RaagasFragmentArgs.fromBundle(requireArguments())
        val viewModelFactory = RaagasFragmentViewModelFactory(arguments.samayId,application)

        viewModel = ViewModelProviders.of(this,viewModelFactory).get(RaagasFragmentViewModel::class.java)
        binding.raagasFragmentViewModel = viewModel
        binding.setLifecycleOwner(this)
        mMainActivity = activity as MainActivity
        val adapter = RagasListAdapter(RagasListener { id ->
            //Toast.makeText(context,"Raga:$id", Toast.LENGTH_LONG).show()
            viewModel.onRagaClicked(id)
        })
        binding.ragaList1.adapter = adapter
        viewModel.ragaList.observe(viewLifecycleOwner,Observer{
            it.let{

                adapter.submitList(it)
            }
        })
        viewModel._samay.observe(viewLifecycleOwner, Observer{
            viewModel.updateMainActivity(activity as IMainActivity)
            viewModel.updateUI()
        })
        viewModel.navigateToRaagaInfo.observe(viewLifecycleOwner, Observer {raga ->
            raga?.let {

                this.findNavController().navigate(RaagasFragmentDirections.actionRaagasFragmentToRaagaInfoFragment(raga,
                    viewModel.samay_key
                ))
                viewModel.onNavigateToRaagaInfo()
            }
        })
        return binding.root
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