package com.example.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    private var _binding: FragmentCrimeListBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "Failed to access binding" }

    private val crimeListViewModel: CrimeListViewModel by viewModels()
    private var job: Job? = null // Uncomment for either manual HW4 experiment.

    // HW4 OBSERVATION: enable while examining initial counts in the experiments.
    // Video 12.2 removes this override in its final cleanup, so it stays commented.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total # of crimes in onCreate: ${crimeListViewModel.crimes.size}")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        binding.crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    // FINAL VERSION: the video 12.2 implementation, plus one HW4 display log.
    // Comment out this whole method before enabling either manual experiment.
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val crimes = crimeListViewModel.loadCrimes()
                binding.crimeRecyclerView.adapter = CrimeListAdapter(crimes)
                Log.d(TAG, "Displayed ${crimes.size} crimes")
            }
        }
    }
    */

    // HW4 EXPERIMENT 1: enable the Job import/property and this callback pair.
    // Keep the final onViewCreated and experiment 2 commented while using it.

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Job launching in onStart")
        job = viewLifecycleOwner.lifecycleScope.launch {
            val crimes = crimeListViewModel.loadCrimes()
            binding.crimeRecyclerView.adapter = CrimeListAdapter(crimes)
            Log.d(TAG, "Displayed ${crimes.size} crimes")
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Job getting canceled in onStop")
        job?.cancel()
    }


    // HW4 EXPERIMENT 2: use this pair instead of experiment 1 and onViewCreated.
    /*
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Job launching in onResume")
        job = viewLifecycleOwner.lifecycleScope.launch {
            val crimes = crimeListViewModel.loadCrimes()
            binding.crimeRecyclerView.adapter = CrimeListAdapter(crimes)
            Log.d(TAG, "Displayed ${crimes.size} crimes")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Job getting canceled in onPause")
        job?.cancel()
    }
    */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
