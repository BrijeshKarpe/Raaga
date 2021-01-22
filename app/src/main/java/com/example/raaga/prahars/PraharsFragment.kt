package com.example.raaga.prahars

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.raaga.R
import com.example.raaga.databinding.FragmentPraharsBinding

@Suppress("DEPRECATION")
class PraharsFragment : Fragment() {

    @SuppressLint("FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentPraharsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_prahars,container,false)
        val application = requireNotNull(this.activity).application
        val viewModelFactory = PraharsFragmentViewModelFactory(application)
        val viewModel = ViewModelProviders.of(this,viewModelFactory).get(PraharsFragmentViewModel::class.java)

        binding.praharsFragmentViewModel = viewModel
        binding.setLifecycleOwner(this)
        setHasOptionsMenu(true)
        val adapter = PraharsListAdapter(PraharsListener { id ->
                        viewModel.onSamayClicked(id)
                    })
        binding.samayList.adapter = adapter
        viewModel._samayList.observe(viewLifecycleOwner, Observer {

            it.let {

                adapter.submitList(it)
            }
        })
        viewModel.navigateToRagas.observe(this,Observer{ samay ->
            samay?.let {

                this.findNavController().navigate(PraharsFragmentDirections.actionPraharsFragmentToRaagasFragment(samay))
                viewModel.onNavigateToRagas()
            }
        })
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
}